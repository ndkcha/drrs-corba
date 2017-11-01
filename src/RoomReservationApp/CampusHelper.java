package RoomReservationApp;


/**
* RoomReservationApp/CampusHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Tuesday, October 31, 2017 6:47:11 o'clock PM EDT
*/

abstract public class CampusHelper
{
  private static String  _id = "IDL:RoomReservationApp/Campus:1.0";

  public static void insert (org.omg.CORBA.Any a, RoomReservationApp.Campus that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RoomReservationApp.Campus extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (RoomReservationApp.CampusHelper.id (), "Campus");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static RoomReservationApp.Campus read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_CampusStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RoomReservationApp.Campus value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static RoomReservationApp.Campus narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof RoomReservationApp.Campus)
      return (RoomReservationApp.Campus)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      RoomReservationApp._CampusStub stub = new RoomReservationApp._CampusStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static RoomReservationApp.Campus unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof RoomReservationApp.Campus)
      return (RoomReservationApp.Campus)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      RoomReservationApp._CampusStub stub = new RoomReservationApp._CampusStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}