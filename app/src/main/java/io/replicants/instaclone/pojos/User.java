package io.replicants.instaclone.pojos;

import java.util.ArrayList;

public class User {

    public static int STATUS_NOT_FOLLOWING = 0;
    public static int STATUS_FOLLOWING = 1;
    public static int STATUS_REQUESTED = 2;


    public String displayName = "";
    public Integer numberPosts = 0;
    public Boolean isPrivate = false;
    public Boolean areFollowing = false;
    public Integer followers = 0;
    public Integer following = 0;
    public Integer followStatusToThem = 0;
    public Integer followStatusToMe = 0;
    public String profileName = "";
    public String profileDesc = "";
    public String profileImage = "";
    public String reason = "";
    public ArrayList<String> followingWhoFollow = new ArrayList<String>();

}
