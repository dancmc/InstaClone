package io.replicants.instaclone.utilities;

import java.util.ArrayList;

public class User {

    public static int STATUS_NOT_FOLLOWING = 0;
    public static int STATUS_FOLLOWING = 1;
    public static int STATUS_REQUESTED = 2;


    String displayName = "";
    Integer numberPosts = 0;
    Boolean isPrivate = false;
    Boolean areFollowing = false;
    Integer followers = 0;
    Integer following = 0;
    Integer followStatusToThem = 0;
    Integer followStatusToMe = 0;
    String profileName = "";
    String profileDesc = "";
    String profileImage = "";
    String reason = "";
    ArrayList<String> followingWhoFollow = new ArrayList<String>();

}
