package RoomReservationApp;


/**
* RoomReservationApp/AvailTimeSlotsHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Tuesday, October 31, 2017 6:47:11 o'clock PM EDT
*/

public final class AvailTimeSlotsHolder implements org.omg.CORBA.portable.Streamable
{
  public RoomReservationApp.AvailTimeSlot value[] = null;

  public AvailTimeSlotsHolder ()
  {
  }

  public AvailTimeSlotsHolder (RoomReservationApp.AvailTimeSlot[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RoomReservationApp.AvailTimeSlotsHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RoomReservationApp.AvailTimeSlotsHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RoomReservationApp.AvailTimeSlotsHelper.type ();
  }

}
