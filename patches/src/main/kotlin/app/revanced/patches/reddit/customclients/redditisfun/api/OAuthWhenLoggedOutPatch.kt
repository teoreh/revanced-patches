package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.reddit.customclients.oAuthWhenLoggedOutPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

import java.util.logging.Logger

val oAuthWhenLoggedOutPatch = oAuthWhenLoggedOutPatch() { clientIdOption ->
    compatibleWith(
        "com.andrewshu.android.reddit",
        "com.andrewshu.android.redditdonation",
    )

    val clientId by stringOption(
        "client-id",
        "default",
        null,
        "OAuth client ID",
        "The Reddit OAuth client ID.",
        true,
    )

    dependsOn(
        // Replace api.reddit.com with oauth.reddit.com for all unauthenticated requests
        replaceStringPatch("api.reddit.com", "oauth.reddit.com"),
    )

    extendWith("extensions/redditisfun/oauth.rve")

    // TODO:
    // Use parameter for Client ID
    // imgur patch? https://gitlab.com/ReVanced/revanced-patches/-/merge_requests/4787/diffs?commit_id=1dabe26866de3d8c70fa40b29ec4b9fd46997866#2ead4d835f9bb14a977ad2aeea49809489a99a10

    execute {
        oauthBearerTokenFingerprint.method.addInstructions(
                0,
                """
                    invoke-static {p1}, Lapp/revanced/extension/redditisfun/oauth/OAuthTokenManager;->attachToken(Ljava/lang/Object;)V
                """,
            )

        oauthExtensionClientIdFingerprint.method.addInstructions(
            0,
            """
            const-string v0, "$clientId"
            return-object v0
        """
        )
    }
}
