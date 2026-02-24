#!/usr/bin/env python3
"""
Improved script to generate Python/gRPC code from .proto files.
Detects changes via file hashes and regenerates only when needed.
"""

import os
import hashlib
import sqlite3
import logging
from pathlib import Path
from grpc_tools import protoc

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


class ProtoBuilder:
    """
    Builds Python/gRPC code from .proto files, with change detection.
    """

    def __init__(self, proto_dir: str = "proto", out_dir: str = "generated", db_file: str = "hashes.db"):
        self.proto_dir = Path(proto_dir).resolve()
        self.out_dir = Path(out_dir).resolve()
        self.db_file = Path(db_file)
        self._ensure_dirs()

    def _ensure_dirs(self):
        """Create output directory if it doesn't exist."""
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _find_proto_files(self) -> list[Path]:
        """Return a list of relative paths (to proto_dir) of all .proto files."""
        if not self.proto_dir.is_dir():
            raise NotADirectoryError(f"Proto directory not found: {self.proto_dir}")
        proto_files = []
        for path in self.proto_dir.rglob("*.proto"):
            rel_path = path.relative_to(self.proto_dir)
            proto_files.append(rel_path)
        return proto_files

    def _compute_file_hash(self, file_path: Path) -> str:
        """Compute MD5 hash of the given file."""
        hash_md5 = hashlib.md5()
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_md5.update(chunk)
        return hash_md5.hexdigest()

    def _get_current_hashes(self) -> dict[str, str]:
        """Return a dict mapping relative proto paths to their current MD5 hash."""
        hashes = {}
        for rel_path in self._find_proto_files():
            full_path = self.proto_dir / rel_path
            hashes[str(rel_path)] = self._compute_file_hash(full_path)
        return hashes

    def _load_stored_hashes(self) -> dict[str, str]:
        """Load previously stored hashes from SQLite DB."""
        conn = sqlite3.connect(self.db_file)
        conn.row_factory = sqlite3.Row
        c = conn.cursor()
        c.execute("CREATE TABLE IF NOT EXISTS hashes (file TEXT PRIMARY KEY, hash TEXT)")
        c.execute("SELECT file, hash FROM hashes")
        rows = c.fetchall()
        conn.close()
        return {row["file"]: row["hash"] for row in rows}

    def _save_hashes(self, hashes: dict[str, str]):
        """Store the given hashes in the DB (replace old entries)."""
        conn = sqlite3.connect(self.db_file)
        c = conn.cursor()
        c.execute("CREATE TABLE IF NOT EXISTS hashes (file TEXT PRIMARY KEY, hash TEXT)")
        # Use INSERT OR REPLACE for simplicity
        for file_rel, hash_val in hashes.items():
            c.execute("INSERT OR REPLACE INTO hashes (file, hash) VALUES (?, ?)", (file_rel, hash_val))
        conn.commit()
        conn.close()

    def _generate_all(self):
        """Run protoc to generate Python/gRPC code for all proto files."""
        proto_files = [str(p) for p in self._find_proto_files()]
        if not proto_files:
            logging.info("No .proto files found. Nothing to generate.")
            return

        logging.info(f"Generating Python code for {len(proto_files)} proto file(s)...")
        # Prepare protoc arguments
        protoc_args = [
            "protoc",  # dummy argv[0]
            f"-I{self.proto_dir}",
            f"--python_out={self.out_dir}",
            f"--grpc_python_out={self.out_dir}",
        ] + proto_files

        # Run protoc
        exit_code = protoc.main(protoc_args)
        if exit_code != 0:
            raise RuntimeError(f"protoc failed with exit code {exit_code}")
        logging.info("Generation completed successfully.")

    def run(self):
        """Main entry point: check for changes and generate if necessary."""
        logging.info("Checking for changes in .proto files...")
        current_hashes = self._get_current_hashes()
        stored_hashes = self._load_stored_hashes()

        # Compare: if any file is new/modified, regenerate all
        if current_hashes != stored_hashes:
            logging.info("Changes detected. Regenerating all code.")
            self._generate_all()
            self._save_hashes(current_hashes)
            logging.info("Hashes updated.")
        else:
            logging.info("No changes detected. Nothing to do.")


if __name__ == "__main__":
    # You can override directories by passing arguments to the constructor
    builder = ProtoBuilder(proto_dir="proto", out_dir="generated", db_file="hashes.db")
    try:
        builder.run()
    except Exception as e:
        logging.error(f"Build failed: {e}")
        raise
