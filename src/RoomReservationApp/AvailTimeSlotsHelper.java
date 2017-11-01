package RoomReservationApp;


/**
* RoomReservationApp/AvailTimeSlotsHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Tuesday, October 31, 2017 6:47:11 o'clock PM EDT
*/

abstract public class AvailTimeSlotsHelper
{
  private static String  _id = "IDL:RoomReservationApp/AvailTimeSlots:1.0";

  public static void insert (org.omg.CORBA.Any a, RoomReservationApp.AvailTimeSlot[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RoomReservationApp.AvailTimeSlot[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = RoomReservationApp.AvailTimeSlotHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (RoomReservationApp.AvailTimeSlotsHelper.id (), "AvailTimeSlots", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static RoomReservationApp.AvailTimeSlot[] read (org.omg.CORBA.portable.InputStream istream)
  {
    RoomReservationApp.AvailTimeSlot value[] = null;
    int _len0 = istream.read_long ();
    value = new RoomReservationApp.AvailTimeSlot[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = RoomReservationApp.AvailTimeSlotHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RoomReservationApp.AvailTimeSlot[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      RoomReservationApp.AvailTimeSlotHelper.write (ostream, value[_i0]);
  }

}