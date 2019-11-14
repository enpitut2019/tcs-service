CREATE TABLE task (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(511),
  category VARCHAR (255),
  deadline TIMESTAMP,
  estimate INTEGER,
  user_id INTEGER REFERENCES users(id),
  finished_at TIMESTAMP,
  created_at TIMESTAMP default CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  is_deleted BOOLEAN NOT NULL
);
