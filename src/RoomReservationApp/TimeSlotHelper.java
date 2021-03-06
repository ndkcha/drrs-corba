package RoomReservationApp;


/**
* RoomReservationApp/TimeSlotHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Wednesday, November 1, 2017 4:35:14 o'clock PM EDT
*/

abstract public class TimeSlotHelper
{
  private static String  _id = "IDL:RoomReservationApp/TimeSlot:1.0";

  public static void insert (org.omg.CORBA.Any a, RoomReservationApp.TimeSlot that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RoomReservationApp.TimeSlot extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [4];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "startTime",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "endTime",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[2] = new org.omg.CORBA.StructMember (
            "bookedBy",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[3] = new org.omg.CORBA.StructMember (
            "bookingId",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (RoomReservationApp.TimeSlotHelper.id (), "TimeSlot", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static RoomReservationApp.TimeSlot read (org.omg.CORBA.portable.InputStream istream)
  {
    RoomReservationApp.TimeSlot value = new RoomReservationApp.TimeSlot ();
    value.startTime = istream.read_string ();
    value.endTime = istream.read_string ();
    value.bookedBy = istream.read_string ();
    value.bookingId = istream.read_string ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RoomReservationApp.TimeSlot value)
  {
    ostream.write_string (value.startTime);
    ostream.write_string (value.endTime);
    ostream.write_string (value.bookedBy);
    ostream.write_string (value.bookingId);
  }

}
