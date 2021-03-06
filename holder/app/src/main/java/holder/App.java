/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package holder;

import express.Express;
import express.utils.Status;
import org.medibloc.vc.VerifiableCredentialException;
import org.medibloc.vc.model.Credential;
import org.medibloc.vc.model.CredentialSubject;
import org.medibloc.vc.verifiable.VerifiableCredential;
import org.medibloc.vc.verifiable.VerifiablePresentation;

public class App {
    public static void main(String[] args) throws Exception {
        // Generate/Register a DID
        Holder holder = new Holder();

        // Request an issuance of a Verifiable Credential to an issuer
        VerifiableCredential vc = holder.getVcFromIssuer();
        printVc(vc);

        // Open a REST server to accept requests from verifiers
        // NOTE: This should be a QR-code communication or something else, rather than the REST server
        //       because the holder would be a mobile app.
        startRestServer(holder);
    }

    private static void startRestServer(Holder holder) {
        Express app = new Express();
        app.get("/vp/verifier/:verifier/nonce/:nonce", (req, res) -> {
            String verifier = req.getParam("verifier");
            String nonce = req.getParam("nonce");

            try {
                VerifiablePresentation vp = holder.createVerifiablePresentation(verifier, nonce);
                res.send(vp.serialize());
            } catch (Exception e) {
                e.printStackTrace();
                res.sendStatus(Status._500);
            }
        });

        int port = 9999;
        System.out.printf("Serving REST: %d\n", port);
        app.listen(port);
    }

    private static void printVc(VerifiableCredential vc) throws VerifiableCredentialException {
        Credential credential = vc.getCredential();
        CredentialSubject cs = credential.getCredentialSubject();
        String hospital = (String) cs.getClaims().get("hospital");
        System.out.printf("hospital: %s\n", hospital);
    }
}
