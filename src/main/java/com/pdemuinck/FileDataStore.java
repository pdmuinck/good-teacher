package com.pdemuinck;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

public class FileDataStore implements DataStore{

  @Override
  public void writeActivity(String data) {
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming", "GoodTeacher");
    File customDir = new File(path);

    if (customDir.exists() || customDir.mkdirs()) {
      try {
        Files.write(new File(String.join(File.separator, path, "activities.csv")).toPath(), data.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(String.format("Cannot write data, because custom dir doesn't exist: %s", path));
    }
  }

  @Override
  public List<String> readActivities() {
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming", "GoodTeacher", "activities.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeActivityBoard(String board, String path) {
    File customDir = new File(path);
    if (customDir.exists() || customDir.mkdirs()) {
      try {
        Files.write(new File(String.join(File.separator, path, "activities.csv")).toPath(), board.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(String.format("Cannot write data, because custom dir doesn't exist: %s", path));
    }
  }
}
