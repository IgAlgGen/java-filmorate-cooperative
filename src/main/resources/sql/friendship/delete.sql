DELETE FROM friendships
WHERE requester_id = :userId AND addressee_id = :friendId;