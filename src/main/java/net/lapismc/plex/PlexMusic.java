package net.lapismc.plex;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

class PlexMusic {

    PlexMusic() {
        File folder = new File("./Music");
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory())
                continue;
            String artist = getArtist(f);
            try {
                FileUtils.moveFileToDirectory(f, new File(folder, artist), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getArtist(File f) {
        AudioFile audioFile;
        String artist = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            artist = tag.getFirst(FieldKey.ARTIST);
            String title = tag.getFirst(FieldKey.TITLE);
            String album = tag.getFirst(FieldKey.ALBUM);

            tag = audioFile.createDefaultTag();
            tag.setField(FieldKey.ALBUM_ARTIST, artist);
            tag.setField(FieldKey.ARTIST, artist);
            tag.setField(FieldKey.TITLE, title);
            tag.setField(FieldKey.ALBUM, album);

            audioFile.setTag(tag);
            audioFile.commit();
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
            e.printStackTrace();
        }
        artist = artist.replaceAll("[\\/:*?\"<>|]", "");
        return !artist.equals("") ? artist : "Unknown Artist";
    }

}
