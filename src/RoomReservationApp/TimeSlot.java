package RoomReservationApp;


/**
* RoomReservationApp/TimeSlot.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Wednesday, November 1, 2017 4:35:14 o'clock PM EDT
*/

public final class TimeSlot implements org.omg.CORBA.portable.IDLEntity
{
  public String startTime = null;
  public String endTime = null;
  public String bookedBy = null;
  public String bookingId = null;

  public TimeSlot ()
  {
  } // ctor

  public TimeSlot (String _startTime, String _endTime, String _bookedBy, String _bookingId)
  {
    startTime = _startTime;
    endTime = _endTime;
    bookedBy = _bookedBy;
    bookingId = _bookingId;
  } // ctor

} // class TimeSlot
