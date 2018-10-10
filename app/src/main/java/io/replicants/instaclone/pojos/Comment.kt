package io.replicants.instaclone.pojos

class Comment {

    var commentID = ""
    var displayName = ""
    var profileImage = ""
    var text = ""
    var timestamp = 0L

    override fun equals(other: Any?): Boolean {
        if(other is Comment){
            return other.commentID == this.commentID
        } else {
            return super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return commentID.hashCode()
    }
}