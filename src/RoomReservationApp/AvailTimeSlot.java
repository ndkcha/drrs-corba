package RoomReservationApp;


/**
* RoomReservationApp/AvailTimeSlot.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Wednesday, November 1, 2017 4:35:14 o'clock PM EDT
*/

public final class AvailTimeSlot implements org.omg.CORBA.portable.IDLEntity
{
  public String campusCode = null;
  public int noOfSlots = (int)0;

  public AvailTimeSlot ()
  {
  } // ctor

  public AvailTimeSlot (String _campusCode, int _noOfSlots)
  {
    campusCode = _campusCode;
    noOfSlots = _noOfSlots;
  } // ctor

} // class AvailTimeSlot
