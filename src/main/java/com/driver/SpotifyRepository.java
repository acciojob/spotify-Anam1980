package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        if(!artists.contains(artistName)){
            artist = new Artist(artistName);
            artists.add(artist);
        }
        else{
            for(Artist art : artists){
                if(art.getName().equals(artistName)){
                    artist = art;
                }
            }
        }
        Album album = new Album(title);
        albums.add(album);
        List<Album> albumList = new ArrayList<>();
        if(artistAlbumMap.containsKey(artist)){
            albumList = artistAlbumMap.get(artist);
        }
        albumList.add(album);
        artistAlbumMap.put(artist, albumList);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        if(!albums.contains(albumName)){
            throw new Exception("Album does not exist");
        }
        Album album = null;
        for (Album album1 : albums){
            if(album1.getTitle().equals(title)){
                album = album1;
            }
        }
        Song song = new Song(title, length);
        songs.add(song);

        List<Song>songList = new ArrayList<>();
        if(albumSongMap.containsKey(album)){
            songList=albumSongMap.get(album);
        }
        songList.add(song);

        albumSongMap.put(album, songList);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist playlist = new Playlist(title);
        List<Song>songList = new ArrayList<>();
        playlists.add(playlist);
        for(Song song : songs){
            if(song.getLength()==length){
                songList.add(song);
            }
        }
        playlistSongMap.put(playlist, songList);

        User user = null;
        for (User user1 : users){
            if(user.getMobile().equals(mobile)){
                user =user1;
            }
        }
        if(user ==null){
            throw new Exception("User does not exist");
        }
        creatorPlaylistMap.put(user, playlist);
        List<User> userList = new ArrayList<>();
        if(playlistListenerMap.containsKey(playlist)){
            userList=playlistListenerMap.get(playlist);
        }
        userList.add(user);
        playlistListenerMap.put(playlist, userList);

        List<Playlist>playlistList  = new ArrayList<>();
        if(userPlaylistMap.containsKey(user)){
            playlistList=userPlaylistMap.get(user);
        }

        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song>songList = new ArrayList<>();

        for(Song song : songs){
            for (String songtitle : songTitles){
                if(song.getTitle().equals(songtitle)){
                   songList.add(song);
                }
            }
        }
        playlistSongMap.put(playlist, songList);

        User user = null;

        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        List<User> userList = new ArrayList<>();
        if(playlistListenerMap.containsKey(playlist)){
            userList = playlistListenerMap.get(playlist);
        }
        userList.add(user);
        playlistListenerMap.put(playlist, userList);

        creatorPlaylistMap.put(user, playlist);
        List<Playlist>playlistList  = new ArrayList<>();
        if(userPlaylistMap.containsKey(user)){
            playlistList=userPlaylistMap.get(user);
        }

        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);


        return playlist;

    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        Playlist playlist = new Playlist(playlistTitle);
        if (playlists.contains(playlist)){
            User user = null;
            for(User user1 : users){
                if(user1.getMobile().equals(mobile)){
                    user = user1;
                }
            }
            if(user == null){
                throw new Exception("User does not exist");
            }

            List<User> userList = new ArrayList<>();
            if(playlistListenerMap.containsKey(playlist)){
                userList = playlistListenerMap.get(playlist);
            }
            boolean isUserAlreadyLiked = false;
            for (User user1 : userList){
                if(user.equals(user1)){
                    isUserAlreadyLiked = true;
                }
            }
            if (isUserAlreadyLiked==false) {
                playlistListenerMap.put(playlist, userList);
            }

            if(!creatorPlaylistMap.containsKey(user)){
                creatorPlaylistMap.put(user, playlist);
            }
            return playlist;
        }
       else{
           throw new Exception("Playlist does not exist");
        }
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        // Find the user with the given mobile number
        User user = getUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }

        // Find the song with the given title
        Song song = getSongByTitle(songTitle);
        if (song == null) {
            throw new Exception("Song does not exist");
        }

        // Check if the user has already liked the song
        if (hasUserLikedSong(user, song)) {
            // User has already liked the song, do nothing
            return song;
        }

        // Add the user to the list of users who liked the song
        addUserToLikedUsers(song, user);

        // Automatically like the corresponding artist of the song
        Artist artist = getArtistForSong(song);
        if (artist != null) {
            // Increment the artist's likes
            artist.setLikes(song.getLikes()+1);
        }

        // Return the song after updating
        return song;
    }

    // Helper method to find a user by mobile number
    private User getUserByMobile(String mobile) {
        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                return user;
            }
        }
        return null;
    }

    // Helper method to find a song by title
    private Song getSongByTitle(String songTitle) {
        for (Song song : songs) {
            if (song.getTitle().equals(songTitle)) {
                return song;
            }
        }
        return null;
    }

    // Helper method to check if a user has already liked a song
    private boolean hasUserLikedSong(User user, Song song) {
        List<User> likedUsers = songLikeMap.get(song);
        return likedUsers != null && likedUsers.contains(user);
    }

    // Helper method to add a user to the list of users who liked a song
    private void addUserToLikedUsers(Song song, User user) {
        List<User> likedUsers = songLikeMap.getOrDefault(song, new ArrayList<>());
        likedUsers.add(user);
        songLikeMap.put(song, likedUsers);
        // Update the number of likes for the song
        song.setLikes(likedUsers.size());
    }

    // Helper method to get the artist associated with a song
    private Artist getArtistForSong(Song song) {
        for (Album album : albumSongMap.keySet()) {
            List<Song> songsInAlbum = albumSongMap.get(album);
            if (songsInAlbum.contains(song)) {
                return getArtistForAlbum(album);
            }
        }
        return null;
    }

    private Artist getArtistForAlbum(Album album) {
        for (Artist artist : artistAlbumMap.keySet()) {
            if (artistAlbumMap.get(artist).contains(album)) {
                return artist;
            }
        }
        return null; // Artist not found
    }


    public String mostPopularArtist() {
        int maxlike =0;
        String Popartist = null;
        for (Artist artist : artists){
            if(artist.getLikes()>maxlike){
                maxlike = artist.getLikes();
                Popartist =artist.getName();
            }
        }
        return Popartist;
    }

    public String mostPopularSong() {
        int maxlike =0;
        String PopSong = null;
        for (Song song : songs){
            if(song.getLikes()>maxlike){
                maxlike = song.getLikes();
                PopSong =song.getTitle();
            }
        }
        return PopSong;
    }
}
