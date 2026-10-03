package skid.ykow.module.setting;

import java.util.ArrayList;
import java.util.List;

public class FriendsSetting extends Setting {
   private final List<String> friends;
   private final List<String> defaultFriends;

   public FriendsSetting(CharSequence name, List<String> defaultFriends) {
      super(name);
      this.defaultFriends = new ArrayList<>(defaultFriends);
      this.friends = new ArrayList<>(defaultFriends);
   }

   public FriendsSetting(CharSequence name) {
      this(name, new ArrayList<>());
   }

   public List<String> getFriends() {
      return new ArrayList<>(this.friends);
   }

   public void addFriend(String name) {
      if (!this.friends.contains(name) && !name.trim().isEmpty()) {
         this.friends.add(name);
      }
   }

   public void removeFriend(String name) {
      this.friends.remove(name);
   }

   public void clearFriends() {
      this.friends.clear();
   }

   public boolean isFriend(String name) {
      return this.friends.contains(name);
   }

   public void setFriends(List<String> friends) {
      this.friends.clear();
      this.friends.addAll(friends);
   }

   public List<String> getDefaultFriends() {
      return new ArrayList<>(this.defaultFriends);
   }

   public void resetValue() {
      this.friends.clear();
      this.friends.addAll(this.defaultFriends);
   }

   public int size() {
      return this.friends.size();
   }
}
