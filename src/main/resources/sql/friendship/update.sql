UPDATE friendships SET status = :status
WHERE requester_id = :userId AND addressee_id = :friendId;