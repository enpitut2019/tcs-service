CREATE TABLE user_device (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  endpoint VARCHAR(512) NOT NULL,
  auth VARCHAR(255) NOT NULL,
  p256dh VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  UNIQUE(endpoint, auth, p256dh)
);
