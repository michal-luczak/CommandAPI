package me.taison;

class MissingCommandException extends RuntimeException {
    private static final String MESSAGE = "Missing command defined in @GetCommand annotation in %s. If you want to use @GetCommand then you need to register command on your own.";

    public MissingCommandException(String className) {
        super(String.format(MESSAGE, className));
    }
}
