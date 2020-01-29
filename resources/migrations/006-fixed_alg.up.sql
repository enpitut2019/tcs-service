CREATE TABLE fixed_alg (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  alg SMALLINT NOT NULL,
  UNIQUE(user_id)
)
