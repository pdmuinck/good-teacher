package com.pdemuinck;

import java.net.MalformedURLException;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FileSystemService {

  public String openFile(Window window){
    try {
      return new FileChooser().showOpenDialog(window).toURI().toURL().toExternalForm();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
