package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

/**
 * Base class for patches that use OAuth when logged out.
 *
 * @param block The patch block. It is called with the client ID option.
 */
fun oAuthWhenLoggedOutPatch(
    block: BytecodePatchBuilder.(Option<String>) -> Unit = {},
) = bytecodePatch(
    name = "Use OAuth when logged out",
    description = "Restores functionality of the app when logged out by fetching OAuth tokens.",
) {
    block(
        stringOption(
            "client-id",
            "",
            null,
            "OAuth client ID",
            "The Reddit OAuth client ID.",
            true,
        ),
    )
}
