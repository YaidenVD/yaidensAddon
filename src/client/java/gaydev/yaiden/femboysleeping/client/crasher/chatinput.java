package gaydev.yaiden.femboysleeping.client.crasher;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.SyncFailedException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.WrongMethodTypeException;
import java.security.UnrecoverableKeyException;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.DOMException;

import gaydev.yaiden.femboysleeping.functions.Fabricatedfuncs;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.level.ServerPlayer;

public class chatinput {

    private Throwable chosen;
    private String check;

    private static final Random RANDOM = new Random();

    private static final Set<String> normal = Set.of(
        "error",
        "crash me"
    );

    private static final Set<String> nuke = Set.of(
        "nuke error",
        "nuclear error",
        "nuclear crash"
    );
    private static final Set<String> mem = Set.of(
        "no mem"
    );

    public void idk() {

        ServerMessageEvents.CHAT_MESSAGE.register((message, player, params) -> {

            String text = message.decoratedContent().getString();

            if (normal.contains(text)) {

                russianRoulette(player);

            } else if (nuke.contains(text)) {

                gaynukes(player);
            } else if (mem.contains(text)) {
                meme(player);
            }
        });
    }

    private void russianRoulette(ServerPlayer player) {

        Throwable[] pool = {

            // Runtime exceptions
            new RuntimeException("yaidensapi said no"),
            new IllegalArgumentException("yaidensapi said no"),
            new IllegalStateException("yaidensapi said no"),
            new NullPointerException("yaidensapi said no"),
            new ArithmeticException("yaidensapi said no"),
            new ClassCastException("yaidensapi said no"),
            new ArrayStoreException("yaidensapi said no"),
            new IndexOutOfBoundsException("yaidensapi said no"),
            new ArrayIndexOutOfBoundsException("yaidensapi said no"),
            new StringIndexOutOfBoundsException("yaidensapi said no"),
            new NegativeArraySizeException("yaidensapi said no"),
            new UnsupportedOperationException("yaidensapi said no"),
            new SecurityException("yaidensapi said no"),

            // Checked exceptions
            new IOException("yaidensapi said no"),
            new EOFException("yaidensapi said no"),
            new FileNotFoundException("yaidensapi said no"),
            new InterruptedException("yaidensapi said no"),
            new CloneNotSupportedException("yaidensapi said no"),
            new ClassNotFoundException("yaidensapi said no"),
            new IllegalAccessException("yaidensapi said no"),
            new InstantiationException("yaidensapi said no"),

            // Errors
            new AssertionError("yaidensapi said no"),
            new InternalError("yaidensapi said no"),
            new UnknownError("yaidensapi said no"),

            // Linkage errors
            new LinkageError("yaidensapi said no"),
            new BootstrapMethodError("yaidensapi said no"),
            new ClassCircularityError("yaidensapi said no"),
            new ClassFormatError("yaidensapi said no"),
            new IncompatibleClassChangeError("yaidensapi said no"),
            new AbstractMethodError("yaidensapi said no"),
            new IllegalAccessError("yaidensapi said no"),
            new InstantiationError("yaidensapi said no"),
            new NoSuchMethodError("yaidensapi said no"),
            new NoClassDefFoundError("yaidensapi said no"),
            new UnsupportedClassVersionError("yaidensapi said no"),
            new VerifyError("yaidensapi said no"),

            // Custom
            new DOMException(
                DOMException.INVALID_CHARACTER_ERR,
                "yaidensapi said no"
            ),
            new OutOfMemoryError(
                "yaidensapi said no"
            ),
            new UnsupportedEncodingException("yaidensapi said no"),
            new StackOverflowError("yaidensapi said no"),
            new SyncFailedException("yaidensapi said no"),
            new UnrecoverableKeyException("yaidensapi said no"),
            new WrongMethodTypeException("yaidensapi said no"),
            new YaidensError(
                "The femboy engine has exploded."
            )
        };

        chooseAndCrash(pool, player);
    }

    private void gaynukes(ServerPlayer player) {

        Throwable[] pool = {

            new LinkageError("yaidensapi said fuck you and nuked you"),
            new BootstrapMethodError("yaidensapi said fuck you and nuked you"),
            new ClassCircularityError("yaidensapi said fuck you and nuked you"),
            new ClassFormatError("yaidensapi said fuck you and nuked you"),
            new IncompatibleClassChangeError("yaidensapi said fuck you and nuked you"),
            new AbstractMethodError("yaidensapi said fuck you and nuked you"),
            new IllegalAccessError("yaidensapi said fuck you and nuked you"),
            new InstantiationError("yaidensapi said fuck you and nuked you"),
            new NoSuchMethodError("yaidensapi said fuck you and nuked you"),
            new NoClassDefFoundError("yaidensapi said fuck you and nuked you"),
            new UnsupportedClassVersionError("yaidensapi said fuck you and nuked you"),
            new VerifyError("yaidensapi said fuck you and nuked you"),
                        new OutOfMemoryError(
                "yaidensapi said no"
            ),
            new UnsupportedEncodingException("yaidensapi said no"),
            new StackOverflowError("yaidensapi said no"),
            new SyncFailedException("yaidensapi said no"),
            new UnrecoverableKeyException("yaidensapi said no"),
            new WrongMethodTypeException("yaidensapi said no"),

            new DOMException(
                DOMException.INVALID_CHARACTER_ERR,
                "yaidensapi said fuck you and nuked you"
            ),
            new OutOfMemoryError(
                "yaidensapi said no"
            ),

            new YaidensError(
                "The femboy engine has exploded."
            )
        };

        chooseAndCrash(pool, player);
    }
    private void meme(ServerPlayer player) {
        Throwable[] pool = {
            new OutOfMemoryError(
                "yaidensapi said no"
            )
        };
        chooseAndCrash(pool, player);
    }

    private void chooseAndCrash(Throwable[] pool, ServerPlayer player) {

        chosen = pool[RANDOM.nextInt(pool.length)];
        check = chosen.toString();

        System.out.println(
            "[YaidensAPI] Russian Roulette selected: "
                + chosen.getClass().getName()
        );

        Fabricatedfuncs.print(player, check);

        /*
         * Only throw it from the server task.
         *
         * Do NOT also throw it directly from the chat event.
         */
        player.getServer().execute(() -> {
            sneakyThrow(chosen);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable throwable)
            throws T {

        throw (T) throwable;
    }
}