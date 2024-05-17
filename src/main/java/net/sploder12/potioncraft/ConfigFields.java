package net.sploder12.potioncraft;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

abstract class Field {
    final String identifier;
    final String comment;

    Field(String identifier, String comment) {
        this.identifier = identifier;
        this.comment = comment;
    }

    abstract void reset();

    protected abstract void writeVal(FileWriter writer) throws IOException;

    void write(FileWriter writer) throws IOException {
        writer.write('#' + comment + '\n');
        writer.write(identifier + '=');
        writeVal(writer);
        writer.write("\n\n");
    }

    protected abstract boolean parseVal(String str);

    boolean load(HashMap<String, String> config) {
        if(config.containsKey(identifier)) {
            return parseVal(config.get(identifier));
        }
        else {
            return false;
        }
    }

    static Boolean getBoolean(Field field) {
        if (field instanceof BooleanField booleanField) {
            return booleanField.val;
        }

        return null;
    }

    static Integer getInteger(Field field) {
        if (field instanceof IntField intField) {
            return intField.val;
        }

        return null;
    }

    static String getString(Field field) {
        if (field instanceof StrField strField) {
            return strField.val;
        }

        return null;
    }
}

class BooleanField extends Field {
    final boolean defualt;
    boolean val;

    BooleanField(boolean defualt, String identifier, String comment) {
        super(identifier, comment);
        this.defualt = defualt;
        this.val = defualt;
    }

    @Override
    void reset() {
        val = defualt;
    }

    @Override
    protected void writeVal(FileWriter writer) throws IOException {
        if (val) {
            writer.write("true\n\n");
        }
        else {
            writer.write("false\n\n");
        }
    }

    @Override
    protected boolean parseVal(String str) {
        if (str.equalsIgnoreCase("true")) {
            val = true;
        }
        else if (str.equalsIgnoreCase("false")) {
            val = false;
        }
        else {
            return false;
        }

        return true;
    }
}

class IntField extends Field {
    final int defualt;
    int val;

    IntField(int defualt, String identifier, String comment) {
        super(identifier, comment);
        this.defualt = defualt;
        this.val = defualt;
    }

    @Override
    void reset() {
        val = defualt;
    }

    @Override
    protected void writeVal(FileWriter writer) throws IOException {
        writer.write(val);
    }

    @Override
    protected boolean parseVal(String str) {
        try {
            val = Integer.parseInt(str);
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }
}

class StrField extends Field {
    final String defualt;
    String val;

    StrField(String defualt, String identifier, String comment) {
        super(identifier, comment);

        this.defualt = defualt;
        this.val = defualt;
    }

    @Override
    void reset() {
        val = defualt;
    }

    @Override
    protected void writeVal(FileWriter writer) throws IOException {
        writer.write(val);
    }

    @Override
    protected boolean parseVal(String str) {
        val = str;
        return true;
    }
}