package com.amplifyframework.datastore.generated.model;


import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.YEAR;
import static android.icu.util.Calendar.getInstance;
import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Appointment type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Appointments")
public final class Appointment implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField NAME = field("name");
  public static final QueryField PHONE = field("phone");
  public static final QueryField TYPE = field("type");
  public static final QueryField TIME = field("time");
  public static final QueryField DURATION = field("duration");
  public static final QueryField DESCRIPTION = field("description");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="String", isRequired = true) String phone;
  private final @ModelField(targetType="String", isRequired = true) String type;
  private final @ModelField(targetType="String", isRequired = true) String time;
  private final @ModelField(targetType="Int", isRequired = true) Integer duration;
  private final @ModelField(targetType="String") String description;
  public String getId() {
      return id;
  }
  
  public String getName() {
      return name;
  }
  
  public String getPhone() {
      return phone;
  }
  
  public String getType() {
      return type;
  }
  
  public String getTime() {
      return time;
  }
  
  public Integer getDuration() {
      return duration;
  }
  
  public String getDescription() {
      return description;
  }
  
  private Appointment(String id, String name, String phone, String type, String time, Integer duration, String description) {
    this.id = id;
    this.name = name;
    this.phone = phone;
    this.type = type;
    this.time = time;
    this.duration = duration;
    this.description = description;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Appointment appointment = (Appointment) obj;
      return ObjectsCompat.equals(getId(), appointment.getId()) &&
              ObjectsCompat.equals(getName(), appointment.getName()) &&
              ObjectsCompat.equals(getPhone(), appointment.getPhone()) &&
              ObjectsCompat.equals(getType(), appointment.getType()) &&
              ObjectsCompat.equals(getTime(), appointment.getTime()) &&
              ObjectsCompat.equals(getDuration(), appointment.getDuration()) &&
              ObjectsCompat.equals(getDescription(), appointment.getDescription());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getName())
      .append(getPhone())
      .append(getType())
      .append(getTime())
      .append(getDuration())
      .append(getDescription())
      .toString()
      .hashCode();
  }
  
  public static NameStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Appointment justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Appointment(
      id,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      name,
      phone,
      type,
      time,
      duration,
      description);
  }

    public boolean isForDay(Calendar thisCalendarDay) {
      Calendar myDate = this.getAppointmentDateTimeAsCalendar();
      if(thisCalendarDay.get(YEAR) == myDate.get(YEAR)
      && thisCalendarDay.get(MONTH) == myDate.get(MONTH)
      && thisCalendarDay.get(DATE) == myDate.get(DATE)) {
          return true;
      }
      else
      {
          return false;

      }
    }


    public interface NameStep {
    PhoneStep name(String name);
  }
  

  public interface PhoneStep {
    TypeStep phone(String phone);
  }
  

  public interface TypeStep {
    TimeStep type(String type);
  }
  

  public interface TimeStep {
    DurationStep time(String time);
  }
  

  public interface DurationStep {
    BuildStep duration(Integer duration);
  }
  

  public interface BuildStep {
    Appointment build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep description(String description);
  }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Calendar getAppointmentDateTimeAsCalendar()
    {
        Calendar cal = getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getStartMins()
    {
        Calendar cal = getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int hours = cal.get(HOUR_OF_DAY);
        int mins = cal.get(MINUTE);

        return hours * 60 + mins;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getEndMins()
    {
        return getStartMins() + duration;

    }

    public String getPurpose()
    {
        try {
            String ret = getType().trim();
            int pos = ret.indexOf("-");
            ret = ret.substring(pos + 1).trim();
            return ret;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getDisplayString() {


        String ret = "";
        if(getType().startsWith("Open"))
        {
            ret = "Available\n" + getPurpose();
        }
        else if(getType().startsWith("Reserved"))
        {
            ret = shorten(getName(), 9) + "\n" + getPhone().substring(2);
        }


        return ret.trim();
    }

    private String shorten(String name, int i) {
      try
      {
          return name.substring(0,i);
      }
      catch(Exception e)
      {
          return name;
      }
    }


    public static class Builder implements NameStep, PhoneStep, TypeStep, TimeStep, DurationStep, BuildStep {
    private String id;
    private String name;
    private String phone;
    private String type;
    private String time;
    private Integer duration;
    private String description;
    @Override
     public Appointment build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Appointment(
          id,
          name,
          phone,
          type,
          time,
          duration,
          description);
    }
    
    @Override
     public PhoneStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public TypeStep phone(String phone) {
        Objects.requireNonNull(phone);
        this.phone = phone;
        return this;
    }
    
    @Override
     public TimeStep type(String type) {
        Objects.requireNonNull(type);
        this.type = type;
        return this;
    }
    
    @Override
     public DurationStep time(String time) {
        Objects.requireNonNull(time);
        this.time = time;
        return this;
    }
    
    @Override
     public BuildStep duration(Integer duration) {
        Objects.requireNonNull(duration);
        this.duration = duration;
        return this;
    }
    
    @Override
     public BuildStep description(String description) {
        this.description = description;
        return this;
    }



    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String name, String phone, String type, String time, Integer duration, String description) {
      super.id(id);
      super.name(name)
        .phone(phone)
        .type(type)
        .time(time)
        .duration(duration)
        .description(description);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder phone(String phone) {
      return (CopyOfBuilder) super.phone(phone);
    }
    
    @Override
     public CopyOfBuilder type(String type) {
      return (CopyOfBuilder) super.type(type);
    }
    
    @Override
     public CopyOfBuilder time(String time) {
      return (CopyOfBuilder) super.time(time);
    }
    
    @Override
     public CopyOfBuilder duration(Integer duration) {
      return (CopyOfBuilder) super.duration(duration);
    }
    
    @Override
     public CopyOfBuilder description(String description) {
      return (CopyOfBuilder) super.description(description);
    }
  }
  
}
