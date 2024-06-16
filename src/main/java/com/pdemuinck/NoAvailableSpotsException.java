package com.pdemuinck;

public class NoAvailableSpotsException extends Exception{
  public NoAvailableSpotsException(){
    super("No spots available for activity");
  }
}
