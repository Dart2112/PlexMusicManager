package net.lapismc.plex;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

class PlexMusic {

    private File folder = new File("./Music");

    PlexMusic() {
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory() || !f.getName().endsWith(".mp3"))
                continue;
            File dir = generateDir(f);
            try {
                System.out.println(f.getName());
                FileUtils.moveFileToDirectory(f, dir, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File generateDir(File f) {
        return new File(folder, getArtist(f) + File.separator + getAlbum(f));
    }

    private String getArtist(File f) {
        AudioFile audioFile;
        String artist = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            artist = tag.getFirst(FieldKey.ARTIST);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        artist = artist.replaceAll("[\\/:*?\"<>|]", "");
        return !artist.equals("") ? artist : "Unknown Artist";
    }

    private String getAlbum(File f) {
        AudioFile audioFile;
        String album = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            album = tag.getFirst(FieldKey.ALBUM);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        album = album.replaceAll("[\\/:*?\"<>|]", "");
        while (album.endsWith(".")) {
            album = album.substring(0, album.length() - 1);
        }
        return !album.equals("") ? album : "Unknown Album";
    }

}
