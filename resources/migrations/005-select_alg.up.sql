CREATE TABLE select_alg (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  alg SMALLINT NOT NULL,
  value INTEGER NOT NULL,
  UNIQUE(user_id, alg)
  )
