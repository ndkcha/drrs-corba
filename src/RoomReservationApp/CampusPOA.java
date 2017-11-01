package RoomReservationApp;


/**
* RoomReservationApp/CampusPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from drrs.idl
* Tuesday, October 31, 2017 10:21:14 o'clock PM EDT
*/

public abstract class CampusPOA extends org.omg.PortableServer.Servant
 implements RoomReservationApp.CampusOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("lookupAdmin", new java.lang.Integer (0));
    _methods.put ("generateAdminId", new java.lang.Integer (1));
    _methods.put ("createRoom", new java.lang.Integer (2));
    _methods.put ("deleteRoom", new java.lang.Integer (3));
    _methods.put ("lookupStudent", new java.lang.Integer (4));
    _methods.put ("generateStudentId", new java.lang.Integer (5));
    _methods.put ("getAvailableTimeSlots", new java.lang.Integer (6));
    _methods.put ("bookRoom", new java.lang.Integer (7));
    _methods.put ("cancelBooking", new java.lang.Integer (8));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {

  // admin interface
       case 0:  // RoomReservationApp/Campus/lookupAdmin
       {
         String id = in.read_string ();
         boolean $result = false;
         $result = this.lookupAdmin (id);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 1:  // RoomReservationApp/Campus/generateAdminId
       {
         String $result = null;
         $result = this.generateAdminId ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // RoomReservationApp/Campus/createRoom
       {
         String date = in.read_string ();
         int roomNo = in.read_long ();
         RoomReservationApp.TimeSlot timeSlots[] = RoomReservationApp.TimeSlotsHelper.read (in);
         boolean $result = false;
         $result = this.createRoom (date, roomNo, timeSlots);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 3:  // RoomReservationApp/Campus/deleteRoom
       {
         String date = in.read_string ();
         int roomNo = in.read_long ();
         RoomReservationApp.TimeSlot timeSlots[] = RoomReservationApp.TimeSlotsHelper.read (in);
         boolean $result = false;
         $result = this.deleteRoom (date, roomNo, timeSlots);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }


  // student interface
       case 4:  // RoomReservationApp/Campus/lookupStudent
       {
         String id = in.read_string ();
         boolean $result = false;
         $result = this.lookupStudent (id);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 5:  // RoomReservationApp/Campus/generateStudentId
       {
         String $result = null;
         $result = this.generateStudentId ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // RoomReservationApp/Campus/getAvailableTimeSlots
       {
         String date = in.read_string ();
         RoomReservationApp.AvailTimeSlotsHolder availTimeSlots = new RoomReservationApp.AvailTimeSlotsHolder ();
         boolean $result = false;
         $result = this.getAvailableTimeSlots (date, availTimeSlots);
         out = $rh.createReply();
         out.write_boolean ($result);
         RoomReservationApp.AvailTimeSlotsHelper.write (out, availTimeSlots.value);
         break;
       }

       case 7:  // RoomReservationApp/Campus/bookRoom
       {
         String studentId = in.read_string ();
         String code = in.read_string ();
         String date = in.read_string ();
         int roomNumber = in.read_long ();
         RoomReservationApp.TimeSlot timeSlot = RoomReservationApp.TimeSlotHelper.read (in);
         String $result = null;
         $result = this.bookRoom (studentId, code, date, roomNumber, timeSlot);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 8:  // RoomReservationApp/Campus/cancelBooking
       {
         String studentId = in.read_string ();
         String bookingId = in.read_string ();
         boolean $result = false;
         $result = this.cancelBooking (studentId, bookingId);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:RoomReservationApp/Campus:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Campus _this() 
  {
    return CampusHelper.narrow(
    super._this_object());
  }

  public Campus _this(org.omg.CORBA.ORB orb) 
  {
    return CampusHelper.narrow(
    super._this_object(orb));
  }


} // class CampusPOA
