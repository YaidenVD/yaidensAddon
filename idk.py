import gzip
import zlib
import struct
import tkinter as tk
import shutil
import time
from pathlib import Path
from tkinter import ttk, filedialog, messagebox, simpledialog


# ============================================================
# NBT TAG TYPES
# ============================================================

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

TAG_NAMES = {
    0: "End",
    1: "Byte",
    2: "Short",
    3: "Int",
    4: "Long",
    5: "Float",
    6: "Double",
    7: "ByteArray",
    8: "String",
    9: "List",
    10: "Compound",
    11: "IntArray",
    12: "LongArray",
}

NAME_TO_TAG = {
    name.lower(): value
    for value, name in TAG_NAMES.items()
}


# ============================================================
# NBT OBJECT
# ============================================================

class NBTTag:

    def __init__(
        self,
        name,
        tag_type,
        value,
        parent=None
    ):
        self.name = name
        self.tag_type = tag_type
        self.value = value
        self.parent = parent

    def type_name(self):
        return TAG_NAMES[self.tag_type]

    def is_container(self):
        return self.tag_type in (
            TAG_LIST,
            TAG_COMPOUND
        )

    def children(self):

        if self.tag_type == TAG_COMPOUND:
            return self.value

        if self.tag_type == TAG_LIST:

            subtype, values = self.value

            result = []

            for i, value in enumerate(values):

                result.append(
                    NBTTag(
                        f"[{i}]",
                        subtype,
                        value,
                        self
                    )
                )

            return result

        return []

    def display_value(self):

        if self.tag_type == TAG_COMPOUND:
            return f"{len(self.value)} tags"

        if self.tag_type == TAG_LIST:

            subtype, values = self.value

            return (
                f"{len(values)} items "
                f"({TAG_NAMES[subtype]})"
            )

        if self.tag_type in (
            TAG_BYTE_ARRAY,
            TAG_INT_ARRAY,
            TAG_LONG_ARRAY
        ):

            if len(self.value) > 10:
                return (
                    str(self.value[:10])
                    + f" ... ({len(self.value)} items)"
                )

            return str(self.value)

        return str(self.value)


# ============================================================
# NBT READER
# ============================================================

class NBTReader:

    def __init__(self, data):

        self.data = data
        self.offset = 0

    def read(self, size):

        if self.offset + size > len(self.data):
            raise ValueError(
                "Unexpected end of NBT data."
            )

        result = self.data[
            self.offset:
            self.offset + size
        ]

        self.offset += size

        return result

    def byte(self):

        return struct.unpack(
            ">b",
            self.read(1)
        )[0]

    def ubyte(self):

        return self.read(1)[0]

    def short(self):

        return struct.unpack(
            ">h",
            self.read(2)
        )[0]

    def int(self):

        return struct.unpack(
            ">i",
            self.read(4)
        )[0]

    def long(self):

        return struct.unpack(
            ">q",
            self.read(8)
        )[0]

    def float(self):

        return struct.unpack(
            ">f",
            self.read(4)
        )[0]

    def double(self):

        return struct.unpack(
            ">d",
            self.read(8)
        )[0]

    def string(self):

        length = struct.unpack(
            ">H",
            self.read(2)
        )[0]

        return self.read(
            length
        ).decode("utf-8")

    def payload(self, tag_type):

        if tag_type == TAG_BYTE:
            return self.byte()

        if tag_type == TAG_SHORT:
            return self.short()

        if tag_type == TAG_INT:
            return self.int()

        if tag_type == TAG_LONG:
            return self.long()

        if tag_type == TAG_FLOAT:
            return self.float()

        if tag_type == TAG_DOUBLE:
            return self.double()

        if tag_type == TAG_BYTE_ARRAY:

            length = self.int()

            if length < 0:
                raise ValueError(
                    "Negative byte array length."
                )

            return list(
                self.read(length)
            )

        if tag_type == TAG_STRING:
            return self.string()

        if tag_type == TAG_LIST:

            subtype = self.ubyte()
            length = self.int()

            if length < 0:
                raise ValueError(
                    "Negative list length."
                )

            values = []

            for _ in range(length):
                values.append(
                    self.payload(subtype)
                )

            return (
                subtype,
                values
            )

        if tag_type == TAG_COMPOUND:

            values = []

            while True:

                child_type = self.ubyte()

                if child_type == TAG_END:
                    break

                child_name = self.string()

                child_value = self.payload(
                    child_type
                )

                child = NBTTag(
                    child_name,
                    child_type,
                    child_value
                )

                values.append(child)

            return values

        if tag_type == TAG_INT_ARRAY:

            length = self.int()

            if length < 0:
                raise ValueError(
                    "Negative int array length."
                )

            if length == 0:
                return []

            return list(
                struct.unpack(
                    f">{length}i",
                    self.read(length * 4)
                )
            )

        if tag_type == TAG_LONG_ARRAY:

            length = self.int()

            if length < 0:
                raise ValueError(
                    "Negative long array length."
                )

            if length == 0:
                return []

            return list(
                struct.unpack(
                    f">{length}q",
                    self.read(length * 8)
                )
            )

        raise ValueError(
            f"Unknown NBT tag type: {tag_type}"
        )

    def tag(self, parent=None):

        tag_type = self.ubyte()

        if tag_type == TAG_END:
            return None

        name = self.string()

        value = self.payload(
            tag_type
        )

        tag = NBTTag(
            name,
            tag_type,
            value,
            parent
        )

        self.fix_parents(tag)

        return tag

    def fix_parents(
        self,
        tag
    ):

        if tag.tag_type == TAG_COMPOUND:

            for child in tag.value:

                child.parent = tag

                self.fix_parents(
                    child
                )


# ============================================================
# NBT WRITER
# ============================================================

class NBTWriter:

    @staticmethod
    def string(value):

        raw = str(value).encode(
            "utf-8"
        )

        if len(raw) > 65535:
            raise ValueError(
                "NBT string is too long."
            )

        return (
            struct.pack(">H", len(raw))
            + raw
        )

    @staticmethod
    def payload(
        tag_type,
        value
    ):

        if tag_type == TAG_BYTE:

            return struct.pack(
                ">b",
                int(value)
            )

        if tag_type == TAG_SHORT:

            return struct.pack(
                ">h",
                int(value)
            )

        if tag_type == TAG_INT:

            return struct.pack(
                ">i",
                int(value)
            )

        if tag_type == TAG_LONG:

            return struct.pack(
                ">q",
                int(value)
            )

        if tag_type == TAG_FLOAT:

            return struct.pack(
                ">f",
                float(value)
            )

        if tag_type == TAG_DOUBLE:

            return struct.pack(
                ">d",
                float(value)
            )

        if tag_type == TAG_BYTE_ARRAY:

            values = [
                int(x)
                for x in value
            ]

            return (
                struct.pack(
                    ">i",
                    len(values)
                )
                + bytes(
                    x & 0xFF
                    for x in values
                )
            )

        if tag_type == TAG_STRING:

            return NBTWriter.string(
                value
            )

        if tag_type == TAG_LIST:

            subtype, values = value

            output = bytearray()

            output.append(
                subtype
            )

            output += struct.pack(
                ">i",
                len(values)
            )

            for item in values:

                output += NBTWriter.payload(
                    subtype,
                    item
                )

            return bytes(output)

        if tag_type == TAG_COMPOUND:

            output = bytearray()

            for child in value:

                output.append(
                    child.tag_type
                )

                output += NBTWriter.string(
                    child.name
                )

                output += NBTWriter.payload(
                    child.tag_type,
                    child.value
                )

            output.append(
                TAG_END
            )

            return bytes(output)

        if tag_type == TAG_INT_ARRAY:

            values = [
                int(x)
                for x in value
            ]

            output = bytearray()

            output += struct.pack(
                ">i",
                len(values)
            )

            if values:

                output += struct.pack(
                    f">{len(values)}i",
                    *values
                )

            return bytes(output)

        if tag_type == TAG_LONG_ARRAY:

            values = [
                int(x)
                for x in value
            ]

            output = bytearray()

            output += struct.pack(
                ">i",
                len(values)
            )

            if values:

                output += struct.pack(
                    f">{len(values)}q",
                    *values
                )

            return bytes(output)

        raise ValueError(
            f"Unknown NBT tag type: {tag_type}"
        )

    @staticmethod
    def root(root):

        return (
            bytes([root.tag_type])
            + NBTWriter.string(root.name)
            + NBTWriter.payload(
                root.tag_type,
                root.value
            )
        )



# ============================================================
# ANVIL REGION (.MCA) SUPPORT
# ============================================================

MCA_SECTOR_SIZE = 4096
MCA_HEADER_SIZE = MCA_SECTOR_SIZE * 2
MCA_CHUNK_COUNT = 32 * 32


class MCAChunk:
    def __init__(self, index, x, z, root, compression_type):
        self.index = index
        self.x = x
        self.z = z
        self.root = root
        self.compression_type = compression_type
        self.timestamp = int(time.time())


class MCAFile:
    """Reader/writer for Minecraft Anvil .mca region files.

    The same format is used by both region/ and entities/ region files.
    """

    def __init__(self, path):
        self.path = Path(path)
        self.chunks = {}
        self.original_locations = [None] * MCA_CHUNK_COUNT
        self.original_timestamps = [0] * MCA_CHUNK_COUNT
        self.load()

    @staticmethod
    def _decompress(payload, compression_type):
        if compression_type == 1:
            return gzip.decompress(payload)
        if compression_type == 2:
            return zlib.decompress(payload)
        if compression_type == 3:
            return payload
        raise ValueError(
            f"Unsupported MCA compression type: {compression_type}"
        )

    @staticmethod
    def _compress(payload, compression_type):
        if compression_type == 1:
            return gzip.compress(payload)
        if compression_type == 2:
            return zlib.compress(payload)
        if compression_type == 3:
            return payload
        raise ValueError(
            f"Unsupported MCA compression type: {compression_type}"
        )

    @staticmethod
    def _parse_chunk(raw):
        if len(raw) < 5:
            raise ValueError("Corrupt MCA chunk: missing length/compression header.")

        length = struct.unpack(">I", raw[:4])[0]
        if length < 1 or length > len(raw) - 4:
            raise ValueError("Corrupt MCA chunk length.")

        compression_type = raw[4]
        compressed = raw[5:4 + length]
        nbt_data = MCAFile._decompress(compressed, compression_type)

        reader = NBTReader(nbt_data)
        root = reader.tag()
        if root is None:
            raise ValueError("MCA chunk contains an empty NBT root.")

        return root, compression_type

    def load(self):
        raw = self.path.read_bytes()

        if len(raw) < MCA_HEADER_SIZE:
            raise ValueError(
                "Not a valid Minecraft MCA file: header is too small."
            )

        locations = raw[:MCA_SECTOR_SIZE]
        timestamps = raw[MCA_SECTOR_SIZE:MCA_HEADER_SIZE]

        for index in range(MCA_CHUNK_COUNT):

            entry = index * 4

            sector_offset = int.from_bytes(
                locations[entry:entry + 3],
                "big"
            )

            sector_count = locations[
                entry + 3
            ]

            timestamp = struct.unpack(
                ">I",
                timestamps[entry:entry + 4]
            )[0]

            # Empty location-table entry
            if sector_offset == 0 or sector_count == 0:
                continue

            self.original_locations[index] = (
                sector_offset,
                sector_count
            )

            self.original_timestamps[index] = timestamp

            start = (
                sector_offset
                * MCA_SECTOR_SIZE
            )

            end = (
                start
                + sector_count
                * MCA_SECTOR_SIZE
            )

            # Invalid location entry
            if (
                sector_offset < 2
                or start >= len(raw)
                or end > len(raw)
            ):
                print(
                    f"Warning: Chunk {index} has an invalid "
                    f"location entry: "
                    f"offset={sector_offset}, "
                    f"sectors={sector_count}"
                )

                self.original_locations[index] = None
                continue

            chunk_raw = raw[
                start:end
            ]

            try:
                root, compression_type = (
                    self._parse_chunk(
                        chunk_raw
                    )
                )

            except Exception as error:
                print(
                    f"Warning: Could not read "
                    f"chunk {index}: {error}"
                )

                continue

            local_x = index % 32
            local_z = index // 32

            root.parent = None

            self.fix_parents(
                root
            )

            chunk = MCAChunk(
                index,
                local_x,
                local_z,
                root,
                compression_type
            )

            chunk.timestamp = timestamp

            self.chunks[index] = chunk

    @staticmethod
    def fix_parents(tag):
        if tag.tag_type == TAG_COMPOUND:
            for child in tag.value:
                child.parent = tag
                MCAFile.fix_parents(child)

    def chunk_items(self):
        return [
            self.chunks[index]
            for index in sorted(self.chunks)
        ]

    def save(self, path=None):
        path = Path(path or self.path)

        # Rebuild the region sequentially. This is valid Anvil layout and
        # avoids trying to fit a modified chunk into its old sector count.
        header = bytearray(MCA_HEADER_SIZE)
        body = bytearray()
        next_sector = 2

        for index in range(MCA_CHUNK_COUNT):
            chunk = self.chunks.get(index)
            if chunk is None:
                continue

            payload = NBTWriter.root(chunk.root)
            compressed = self._compress(
                payload,
                chunk.compression_type
            )

            # Chunk length includes the compression byte.
            chunk_length = len(compressed) + 1
            total = 4 + chunk_length
            sector_count = (total + MCA_SECTOR_SIZE - 1) // MCA_SECTOR_SIZE

            if sector_count > 255:
                raise ValueError(
                    f"Chunk {chunk.x},{chunk.z} is too large for an MCA region."
                )

            padded = bytearray(sector_count * MCA_SECTOR_SIZE)
            struct.pack_into(">I", padded, 0, chunk_length)
            padded[4] = chunk.compression_type
            padded[5:5 + len(compressed)] = compressed

            entry = index * 4
            if next_sector > 0xFFFFFF:
                raise ValueError("MCA file has exceeded the maximum sector offset.")

            header[entry:entry + 3] = next_sector.to_bytes(3, "big")
            header[entry + 3] = sector_count

            timestamp = int(time.time())
            struct.pack_into(
                ">I",
                header,
                MCA_SECTOR_SIZE + entry,
                timestamp
            )

            body.extend(padded)
            next_sector += sector_count

        path.write_bytes(bytes(header) + bytes(body))
        self.path = path


def load_mca(path):
    return MCAFile(path)


# ============================================================
# FILE LOADING
# ============================================================

def load_nbt(path):

    raw = Path(path).read_bytes()

    compression = "raw"

    if raw.startswith(
        b"\x1f\x8b"
    ):

        raw = gzip.decompress(
            raw
        )

        compression = "gzip"

    else:

        try:

            decoded = zlib.decompress(
                raw
            )

            raw = decoded
            compression = "zlib"

        except zlib.error:

            pass

    reader = NBTReader(
        raw
    )

    root = reader.tag()

    if root is None:
        raise ValueError(
            "Invalid NBT root."
        )

    return root, compression


def save_nbt(
    path,
    root,
    compression
):

    data = NBTWriter.root(
        root
    )

    if compression == "gzip":

        data = gzip.compress(
            data
        )

    elif compression == "zlib":

        data = zlib.compress(
            data
        )

    Path(path).write_bytes(
        data
    )


# ============================================================
# GUI
# ============================================================

class NBTEditor(tk.Tk):

    def __init__(self):

        super().__init__()

        self.title(
            "Minecraft NBT Editor"
        )

        self.geometry(
            "1150x700"
        )

        self.minsize(
            800,
            500
        )

        self.root_tag = None
        self.current = None
        self.current_file = None
        self.compression = "raw"
        self.mca_file = None
        self.mca_chunk = None

        self.history = []

        self.create_gui()

    # --------------------------------------------------------
    # GUI
    # --------------------------------------------------------

    def create_gui(self):

        toolbar = ttk.Frame(
            self,
            padding=8
        )

        toolbar.pack(
            fill="x"
        )

        ttk.Button(
            toolbar,
            text="Open",
            command=self.open_file
        ).pack(
            side="left"
        )

        ttk.Button(
            toolbar,
            text="Save",
            command=self.save_file
        ).pack(
            side="left",
            padx=4
        )

        ttk.Button(
            toolbar,
            text="Save As",
            command=self.save_as
        ).pack(
            side="left"
        )

        ttk.Button(
            toolbar,
            text="Backup + Save",
            command=self.backup_save
        ).pack(
            side="left",
            padx=4
        )

        ttk.Button(
            toolbar,
            text="Reload",
            command=self.reload_file
        ).pack(
            side="left",
            padx=4
        )

        ttk.Separator(
            toolbar,
            orient="vertical"
        ).pack(
            side="left",
            fill="y",
            padx=10
        )

        ttk.Label(
            toolbar,
            text="Search:"
        ).pack(
            side="left"
        )

        self.search_box = ttk.Entry(
            toolbar,
            width=30
        )

        self.search_box.pack(
            side="left",
            padx=5
        )

        ttk.Button(
            toolbar,
            text="Find",
            command=self.search
        ).pack(
            side="left"
        )

        self.path_label = ttk.Label(
            self,
            text="No file loaded"
        )

        self.path_label.pack(
            fill="x",
            padx=8
        )

        self.location_label = ttk.Label(
            self,
            text="Root"
        )

        self.location_label.pack(
            fill="x",
            padx=8,
            pady=4
        )

        # Main table

        frame = ttk.Frame(
            self
        )

        frame.pack(
            fill="both",
            expand=True,
            padx=8
        )

        self.table = ttk.Treeview(
            frame,
            columns=(
                "name",
                "type",
                "value"
            ),
            show="headings"
        )

        self.table.heading(
            "name",
            text="Name"
        )

        self.table.heading(
            "type",
            text="Type"
        )

        self.table.heading(
            "value",
            text="Value"
        )

        self.table.column(
            "name",
            width=280
        )

        self.table.column(
            "type",
            width=130
        )

        self.table.column(
            "value",
            width=650
        )

        scrollbar = ttk.Scrollbar(
            frame,
            orient="vertical",
            command=self.table.yview
        )

        self.table.configure(
            yscrollcommand=scrollbar.set
        )

        self.table.pack(
            side="left",
            fill="both",
            expand=True
        )

        scrollbar.pack(
            side="right",
            fill="y"
        )

        self.table.bind(
            "<Double-1>",
            lambda event:
            self.open_selected()
        )

        self.bind(
            "<F5>",
            lambda event: self.reload_file()
        )

        # Bottom buttons

        buttons = ttk.Frame(
            self,
            padding=8
        )

        buttons.pack(
            fill="x"
        )

        for text, command in [
            ("Back", self.go_back),
            ("Open", self.open_selected),
            ("Add", self.add_tag),
            ("Edit", self.edit_tag),
            ("Rename", self.rename_tag),
            ("Delete", self.delete_tag),
            ("Copy", self.copy_value),
        ]:

            ttk.Button(
                buttons,
                text=text,
                command=command
            ).pack(
                side="left",
                padx=2
            )

        self.status = ttk.Label(
            self,
            text="Ready",
            relief="sunken",
            anchor="w"
        )

        self.status.pack(
            fill="x",
            padx=8,
            pady=(0, 5)
        )

    # --------------------------------------------------------
    # Refresh table
    # --------------------------------------------------------

    def refresh(self):

        for item in self.table.get_children():

            self.table.delete(
                item
            )

        if self.current is None:
            return

        for index, tag in enumerate(
            self.current.children()
        ):

            self.table.insert(
                "",
                "end",
                iid=str(index),
                values=(
                    tag.name,
                    tag.type_name(),
                    tag.display_value()
                )
            )

        self.update_location()

    def update_location(self):

        if self.current is None:
            return

        names = []

        node = self.current

        while node is not None:

            names.append(
                node.name
            )

            node = node.parent

        names.reverse()

        self.location_label.config(
            text=" > ".join(names)
        )

    # --------------------------------------------------------
    # File operations
    # --------------------------------------------------------

    def open_file(self):

        path = filedialog.askopenfilename(
            title="Open Minecraft NBT / Region",
            filetypes=[
                ("Minecraft files", "*.nbt *.dat *.mca"),
                ("NBT files", "*.nbt"),
                ("DAT files", "*.dat"),
                ("Region files", "*.mca"),
                ("All files", "*.*")
            ]
        )

        if not path:
            return

        try:
            if Path(path).suffix.lower() == ".mca":
                self.mca_file = load_mca(path)
                self.mca_chunk = None
                self.root_tag = None
                self.current = None
                self.current_file = Path(path)
                self.history.clear()

                self.path_label.config(
                    text=f"{path} [Anvil region: {len(self.mca_file.chunks)} chunks]"
                )
                self.show_mca_chunks()
                self.status.config(text="MCA region loaded.")
                return

            self.mca_file = None
            self.mca_chunk = None

            root, compression = load_nbt(path)

            self.root_tag = root
            self.current = root
            self.current_file = Path(path)
            self.compression = compression
            self.history.clear()

            self.path_label.config(
                text=f"{path} [{compression}]"
            )
            self.status.config(text="Loaded successfully.")
            self.refresh()

        except Exception as error:
            messagebox.showerror(
                "NBT Error",
                str(error)
            )

    def show_mca_chunks(self):
        for item in self.table.get_children():
            self.table.delete(item)

        if self.mca_file is None:
            return

        for chunk in self.mca_file.chunk_items():
            root = chunk.root
            self.table.insert(
                "",
                "end",
                iid=str(chunk.index),
                values=(
                    f"Chunk [{chunk.x}, {chunk.z}]",
                    "NBT Compound" if root.tag_type == TAG_COMPOUND else root.type_name(),
                    root.display_value()
                )
            )

        self.location_label.config(
            text=f"Region: {self.current_file.name} > Chunks"
        )

    def save_file(self):

        if self.mca_file is not None:
            try:
                self.mca_file.save(self.current_file)
                self.status.config(text="MCA region saved.")
            except Exception as error:
                messagebox.showerror("Save Error", str(error))
            return

        if self.root_tag is None or self.current_file is None:
            return

        try:
            save_nbt(
                self.current_file,
                self.root_tag,
                self.compression
            )
            self.status.config(text="Saved.")
        except Exception as error:
            messagebox.showerror("Save Error", str(error))

    def save_as(self):

        if self.mca_file is not None:
            path = filedialog.asksaveasfilename(
                title="Save MCA As",
                defaultextension=".mca",
                filetypes=[
                    ("Region files", "*.mca"),
                    ("All files", "*.*")
                ]
            )

            if not path:
                return

            try:
                self.mca_file.save(path)
                self.current_file = Path(path)
                self.path_label.config(
                    text=f"{path} [Anvil region: {len(self.mca_file.chunks)} chunks]"
                )
                self.status.config(text="MCA region saved as new file.")
            except Exception as error:
                messagebox.showerror("Save Error", str(error))
            return

        if self.root_tag is None:
            return

        path = filedialog.asksaveasfilename(
            title="Save NBT As",
            defaultextension=".nbt",
            filetypes=[
                ("NBT files", "*.nbt"),
                ("DAT files", "*.dat"),
                ("All files", "*.*")
            ]
        )

        if not path:
            return

        try:
            save_nbt(path, self.root_tag, self.compression)
            self.current_file = Path(path)
            self.status.config(text=f"Saved as {path}")
        except Exception as error:
            messagebox.showerror("Save Error", str(error))

    def backup_save(self):

        if self.current_file is None:
            return

        try:
            backup = Path(str(self.current_file) + ".bak")
            shutil.copy2(self.current_file, backup)
            self.save_file()
            self.status.config(
                text=f"Backup created: {backup.name}"
            )
        except Exception as error:
            messagebox.showerror("Backup Error", str(error))

    def reload_file(self):

        if self.current_file is None:
            messagebox.showinfo(
                "Reload",
                "No file is currently loaded."
            )
            return

        if not messagebox.askyesno(
            "Reload",
            f"Reload '{self.current_file.name}' from disk?\n"
            "Any unsaved changes will be lost."
        ):
            return

        path = self.current_file

        try:
            if path.suffix.lower() == ".mca":
                self.mca_file = load_mca(path)
                self.mca_chunk = None
                self.root_tag = None
                self.current = None
                self.history.clear()

                self.path_label.config(
                    text=f"{path} [Anvil region: {len(self.mca_file.chunks)} chunks]"
                )
                self.show_mca_chunks()
                self.status.config(text="MCA region reloaded.")
                return

            self.mca_file = None
            self.mca_chunk = None

            root, compression = load_nbt(path)

            self.root_tag = root
            self.current = root
            self.compression = compression
            self.history.clear()

            self.path_label.config(
                text=f"{path} [{compression}]"
            )
            self.status.config(text="Reloaded from disk.")
            self.refresh()

        except Exception as error:
            messagebox.showerror(
                "Reload Error",
                str(error)
            )

    # --------------------------------------------------------
    # Selection
    # --------------------------------------------------------

    def selected(self):

        selection = self.table.selection()
        if not selection:
            return None

        if self.mca_file is not None and self.current is None:
            index = int(selection[0])
            return self.mca_file.chunks.get(index)

        index = int(selection[0])
        children = self.current.children()

        if index >= len(children):
            return None

        return children[index]

    # --------------------------------------------------------
    # Navigation
    # --------------------------------------------------------

    def open_selected(self):

        selected = self.selected()
        if selected is None:
            return

        if self.mca_file is not None and self.current is None:
            self.mca_chunk = selected
            self.root_tag = selected.root
            self.current = selected.root
            self.history.clear()
            self.refresh()
            self.location_label.config(
                text=(
                    f"Region: {self.current_file.name} > "
                    f"Chunk [{selected.x}, {selected.z}]"
                )
            )
            return

        tag = selected

        if not tag.is_container():
            messagebox.showinfo(
                "Open",
                "This tag has no children."
            )
            return

        self.history.append(self.current)
        self.current = tag
        self.search_box.delete(0, "end")
        self.refresh()

    def go_back(self):

        if self.mca_file is not None and self.current is not None:
            if self.history:
                self.current = self.history.pop()
                self.refresh()
                return

            self.current = None
            self.root_tag = None
            self.mca_chunk = None
            self.show_mca_chunks()
            return

        if not self.history:
            return

        self.current = self.history.pop()
        self.refresh()

    # --------------------------------------------------------
    # Edit
    # --------------------------------------------------------

    def edit_tag(self):

        tag = self.selected()

        if tag is None:
            return

        if tag.tag_type in (
            TAG_COMPOUND,
            TAG_LIST
        ):

            messagebox.showinfo(
                "Edit",
                "Open the container to edit "
                "its contents."
            )

            return

        value = simpledialog.askstring(
            "Edit Value",
            (
                f"{tag.name} "
                f"({tag.type_name()}):"
            ),
            initialvalue=str(
                tag.value
            ),
            parent=self
        )

        if value is None:
            return

        try:

            if tag.tag_type in (
                TAG_BYTE,
                TAG_SHORT,
                TAG_INT,
                TAG_LONG
            ):

                tag.value = int(
                    value,
                    0
                )

            elif tag.tag_type in (
                TAG_FLOAT,
                TAG_DOUBLE
            ):

                tag.value = float(
                    value
                )

            elif tag.tag_type == TAG_STRING:

                tag.value = value

            elif tag.tag_type in (
                TAG_BYTE_ARRAY,
                TAG_INT_ARRAY,
                TAG_LONG_ARRAY
            ):

                cleaned = value.strip(
                    "[] "
                )

                if not cleaned:

                    tag.value = []

                else:

                    tag.value = [
                        int(
                            x.strip(),
                            0
                        )
                        for x in cleaned.split(",")
                    ]

            else:

                raise ValueError(
                    "Unsupported value type."
                )

            self.refresh()

        except Exception as error:

            messagebox.showerror(
                "Edit Error",
                str(error)
            )

    # --------------------------------------------------------
    # Rename
    # --------------------------------------------------------

    def rename_tag(self):

        tag = self.selected()

        if tag is None:
            return

        if self.current.tag_type != TAG_COMPOUND:

            messagebox.showinfo(
                "Rename",
                "Only Compound children "
                "can be renamed."
            )

            return

        name = simpledialog.askstring(
            "Rename",
            "New name:",
            initialvalue=tag.name,
            parent=self
        )

        if name is None:
            return

        if not name:

            messagebox.showerror(
                "Rename",
                "Name cannot be empty."
            )

            return

        tag.name = name

        self.refresh()

    # --------------------------------------------------------
    # Delete
    # --------------------------------------------------------

    def delete_tag(self):

        tag = self.selected()

        if tag is None:
            return

        if not messagebox.askyesno(
            "Delete",
            f"Delete '{tag.name}'?",
            parent=self
        ):
            return

        if self.current.tag_type == TAG_COMPOUND:

            self.current.value.remove(
                tag
            )

        elif self.current.tag_type == TAG_LIST:

            selection = self.table.selection()

            index = int(
                selection[0]
            )

            self.current.value[1].pop(
                index
            )

        self.refresh()

    # --------------------------------------------------------
    # Add
    # --------------------------------------------------------

    def add_tag(self):

        if self.current is None:
            return

        if self.current.tag_type != TAG_COMPOUND:

            messagebox.showinfo(
                "Add",
                "You can only add tags "
                "inside a Compound."
            )

            return

        name = simpledialog.askstring(
            "Add Tag",
            "Name:",
            parent=self
        )

        if name is None:
            return

        type_text = simpledialog.askstring(
            "Add Tag",
            (
                "Type:\n"
                "Byte\n"
                "Short\n"
                "Int\n"
                "Long\n"
                "Float\n"
                "Double\n"
                "ByteArray\n"
                "String\n"
                "List\n"
                "Compound\n"
                "IntArray\n"
                "LongArray"
            ),
            parent=self
        )

        if type_text is None:
            return

        tag_type = NAME_TO_TAG.get(
            type_text.lower()
        )

        if tag_type is None or tag_type == TAG_END:

            messagebox.showerror(
                "Add",
                "Invalid NBT type."
            )

            return

        value = self.default_value(
            tag_type
        )

        self.current.value.append(
            NBTTag(
                name,
                tag_type,
                value,
                self.current
            )
        )

        self.refresh()

    @staticmethod
    def default_value(tag_type):

        if tag_type in (
            TAG_BYTE,
            TAG_SHORT,
            TAG_INT,
            TAG_LONG
        ):
            return 0

        if tag_type in (
            TAG_FLOAT,
            TAG_DOUBLE
        ):
            return 0.0

        if tag_type == TAG_STRING:
            return ""

        if tag_type in (
            TAG_BYTE_ARRAY,
            TAG_INT_ARRAY,
            TAG_LONG_ARRAY
        ):
            return []

        if tag_type == TAG_COMPOUND:
            return []

        if tag_type == TAG_LIST:
            return (
                TAG_STRING,
                []
            )

        raise ValueError(
            "Invalid NBT type."
        )

    # --------------------------------------------------------
    # Copy
    # --------------------------------------------------------

    def copy_value(self):

        tag = self.selected()

        if tag is None:
            return

        self.clipboard_clear()

        self.clipboard_append(
            tag.display_value()
        )

        self.status.config(
            text="Copied."
        )

    # --------------------------------------------------------
    # Search
    # --------------------------------------------------------

    def search(self):

        query = (
            self.search_box
            .get()
            .strip()
            .lower()
        )

        if not query:
            self.refresh()
            return

        for item in self.table.get_children():

            values = self.table.item(
                item,
                "values"
            )

            text = " ".join(
                str(x)
                for x in values
            ).lower()

            if query in text:

                self.table.selection_set(
                    item
                )

                self.table.focus(
                    item
                )

                self.table.see(
                    item
                )

                self.status.config(
                    text=f"Found: {query}"
                )

                return

        self.status.config(
            text=f"No match: {query}"
        )


# ============================================================
# START
# ============================================================

if __name__ == "__main__":

    app = NBTEditor()

    app.mainloop()