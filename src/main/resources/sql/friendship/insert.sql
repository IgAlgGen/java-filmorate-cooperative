MERGE INTO friendships (requester_id, addressee_id, status)
KEY (requester_id, addressee_id)
VALUES (:userId, :friendId, :status);