CREATE TABLE user_token (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  token VARCHAR(255) NOT NULL,
  created_at TIMESTAMP default CURRENT_TIMESTAMP
);
