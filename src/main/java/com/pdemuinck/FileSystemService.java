package com.pdemuinck;

import java.io.File;
import java.net.MalformedURLException;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FileSystemService {

  public String openFile(Window window){
    try {
      File file = new FileChooser().showOpenDialog(window);
      if(file != null){
        return file.toURI().toURL().toExternalForm();
      } else {
        throw new RuntimeException("No file found");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
