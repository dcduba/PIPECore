package utils;

import uk.ac.imperial.pipe.models.component.token.ColoredToken;
import uk.ac.imperial.pipe.models.component.token.Token;

import java.awt.Color;

/**
 * Static class for useful token utilities
 */
public class TokenUtils {
    private TokenUtils() {
    }

    public static Token createDefaultToken() {
        return new ColoredToken("Default", new Color(0, 0, 0));
    }
}
