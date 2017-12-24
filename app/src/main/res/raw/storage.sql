CREATE TABLE manga (
	id INTEGER PRIMARY KEY,
	name TEXT NOT NULL,
	summary TEXT,
	genres TEXT,
	url TEXT NOT NULL,
	thumbnail TEXT,
	provider TEXT NOT NULL,
	status INTEGER,
	rating INTEGER
);

CREATE TABLE history (
	id INTEGER PRIMARY KEY,
	manga_id INTEGER NOT NULL,
	chapter_id INTEGER NOT NULL,
	page_id INTEGER NOT NULL,
	updated_at INTEGER NOT NULL,
	reader_preset INTEGER,
	total_chapters INTEGER,
	total_pages_in_chapter INTEGER,
	FOREIGN KEY (manga_id) REFERENCES manga(id)
)