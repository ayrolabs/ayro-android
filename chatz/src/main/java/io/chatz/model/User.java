package io.chatz.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class User implements Serializable {

  private static final long serialVersionUID = 8121929561544316347L;

  private String uid;
  private String firstName;
  private String lastName;
  private String photoUrl;
  private String email;
  private Date signUpDate;
  private Map<String, String> properties;

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getSignUpDate() {
    return signUpDate;
  }

  public void setSignUpDate(Date signUpDate) {
    this.signUpDate = signUpDate;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getFullName() {
    if(firstName != null) {
      return firstName + (lastName != null ? " " + lastName : "");
    } else if(lastName != null) {
      return lastName;
    } else {
      return "";
    }
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return uid != null ? uid.equals(user.uid) : user.uid == null;
  }

  @Override
  public int hashCode() {
    return uid != null ? uid.hashCode() : 0;
  }
}