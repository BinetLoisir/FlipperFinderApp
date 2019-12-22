package com.pinmyballs.service.parse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.fragment.FragmentActionsFlipper.FragmentActionCallback;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.ParseFactory;
import com.pinmyballs.service.base.BaseCommentaireService;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.ProgressBarHandler;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParseFlipperService {

    private static final String TAG = "ParseFlipperService";
    private FragmentActionCallback mFragmentCallback;

    public ParseFlipperService(FragmentActionCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }

    /**
     * Retourne tous les flipper à partir du cloud
     *
     * @return List<ModeleFlipper>
     */
    public List<Flipper> getAllFlipper() {
		/*
		   List<ParseObject> listePo = new ArrayList<ParseObject>();
		   ParseQuery query = new ParseQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
		   try {
		   query.setLimit(5000);
		   listePo = query.find();
		   } catch (ParseException e1) {
		   e1.printStackTrace();
		   }
		   for (ParseObject po : listePo) {
		   Flipper flipper = new Flipper(po.getLong("flipId"),
		   po.getLong(FlipperDatabaseHandler.FLIPPER_MODELE),
		   po.getLong(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E),
		   po.getLong(FlipperDatabaseHandler.FLIPPER_ENSEIGNE));
		   listeFlipper.add(flipper);
		   }
		   */
        return new ArrayList<Flipper>();
    }

    /**
     * Retourne la liste des flippers à mettre à jour à partir d'une date donnée.
     *
     * @param dateDerniereMaj
     * @return
     */
    public List<Flipper> getMajFlipperByDate(String dateDerniereMaj) {
        List<Flipper> listeFlipper = new ArrayList<Flipper>();

        List<ParseObject> listePo;
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        try {
            query.setLimit(5000);
            query.whereGreaterThanOrEqualTo(FlipperDatabaseHandler.FLIPPER_DATMAJ, dateDerniereMaj);
            listePo = query.find();
        } catch (ParseException e1) {
            e1.printStackTrace();
            return null;
        }
        for (ParseObject po : listePo) {
            Flipper flipper = new Flipper(po.getLong(FlipperDatabaseHandler.FLIPPER_ID),
                    po.getLong(FlipperDatabaseHandler.FLIPPER_MODELE),
                    po.getLong(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E),
                    po.getLong(FlipperDatabaseHandler.FLIPPER_ENSEIGNE),
                    po.getBoolean(FlipperDatabaseHandler.FLIPPER_ACTIF),
                    po.getString(FlipperDatabaseHandler.FLIPPER_DATMAJ));
            listeFlipper.add(flipper);
        }
        return listeFlipper;
    }

    public boolean updateDateFlipper(final Context pContext, final Flipper flipper, final String dateToSave) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID, flipper.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    objects.get(0).put(FlipperDatabaseHandler.FLIPPER_DATMAJ, dateToSave);
                    objects.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                mProgressBarHandler.hide();
                                BaseFlipperService baseFlipperService = new BaseFlipperService();
                                flipper.setDateMaj(dateToSave);
                                baseFlipperService.majFlipper(flipper, pContext);
                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastValidationCloudOK), Toast.LENGTH_SHORT);
                                toast.show();
                                if (mFragmentCallback != null) {
                                    mFragmentCallback.onTaskDone();
                                }
                            } else {
                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastValidationCloudKO), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });
        return true;
    }

    public boolean supprimeFlipper(final Context pContext, final Flipper ancienflipper) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID, ancienflipper.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    // On vérifie d'abord que le flipper n'était pas déjà désactivé
                    if (!objects.get(0).getBoolean(FlipperDatabaseHandler.FLIPPER_ACTIF)) {
                        new AlertDialog.Builder(pContext).setTitle("Déjà supprimé!").setMessage("Le flipper a déjà été retiré par un autre utilisateur. Mettez à jour votre base de flipper pour voir les dernières modifications.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                    } else {

                        // On passe le flipper inactif et on update la date de màj
                        objects.get(0).put(FlipperDatabaseHandler.FLIPPER_ACTIF, false);
                        objects.get(0).put(FlipperDatabaseHandler.FLIPPER_DATMAJ, ancienflipper.getDateMaj());


                        // On met le tout dans une liste
                        List<ParseObject> listParseToSave = new ArrayList<ParseObject>();
                        listParseToSave.add(objects.get(0));

                        // Et on balance in da cloud!
                        ParseObject.saveAllInBackground(listParseToSave, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                //mProgressBarHandler.hide();
                                if (e == null) {
                                    // Ca s'est bien passé, on sauvegarde les flippers
                                    List<Flipper> listBaseToSave = new ArrayList<Flipper>();
                                    listBaseToSave.add(ancienflipper);

                                    BaseFlipperService baseFlipperService = new BaseFlipperService();
                                    baseFlipperService.majListeFlipper(listBaseToSave, pContext);


                                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupRetraitFlipOK), Toast.LENGTH_SHORT);
                                    toast.show();
                                    if (mFragmentCallback != null) {
                                        mFragmentCallback.onTaskDone();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupRetraitFlipKO), Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    }
                }
            }
        });

        return true;
    }

    public boolean modifieEtatFlipper(final Context pContext, final Flipper flipper) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID, flipper.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                Date datedujour = new Date();
                String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(datedujour);
                SharedPreferences settings;
                settings = pContext.getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
                String pseudo = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "SYS");

                final Commentaire commentaireUpdate = new Commentaire();
                commentaireUpdate.setId(datedujour.getTime());
                commentaireUpdate.setFlipperId(flipper.getId());
                commentaireUpdate.setFlipper(flipper);
                commentaireUpdate.setDate(dateMaj);
                commentaireUpdate.setPseudo(pseudo);
                commentaireUpdate.setActif(true);

                List<Commentaire> associatedComments;
                BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
                associatedComments = baseCommentaireService.getCommentaireByFlipperId(pContext, flipper.getId());

                if (objects != null && objects.size() > 0) {
                    //List de ParseObjects à créer, mettre à jour
                    List<ParseObject> listParseToSave = new ArrayList<ParseObject>();

                    ParseObject flipPO = objects.get(0);
                    // Si le flip est désactivé, on l'active, on rajoute un commentaire et on réactive les commentaires du flip
                    if (!flipPO.getBoolean(FlipperDatabaseHandler.FLIPPER_ACTIF)) {
                        // On passe le flipper actif et on update la date de màj
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_ACTIF, true);
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_DATMAJ, flipper.getDateMaj());
                        commentaireUpdate.setTexte("Réinstallé");
                        commentaireUpdate.setType(Commentaire.TYPE_REINSTALL);
                        //On reactive les commentaires associés
                        if (associatedComments.size() >0){
                            for(Commentaire comment : associatedComments){
                                ParseObject commentPO = new ParseFactory().getParseObject(comment);
                                commentPO.put(FlipperDatabaseHandler.COMM_ACTIF, true);
                                listParseToSave.add(commentPO);
                            }
                        }

                    } else {
                        // On passe le flipper inactif et on update la date de màj
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_ACTIF, false);
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_DATMAJ, flipper.getDateMaj());
                        commentaireUpdate.setTexte("Supprimé");
                        commentaireUpdate.setType(Commentaire.TYPE_DELETE);
                        //On desactive les commentaires associés
                        if (associatedComments.size() >0){
                            for(Commentaire comment : associatedComments){
                                ParseObject commentPO = new ParseFactory().getParseObject(comment);
                                commentPO.put(FlipperDatabaseHandler.COMM_ACTIF, false);
                                listParseToSave.add(commentPO);
                            }
                        }
                    }
                    // On ajoute le flipper mis à jour à la liste
                    listParseToSave.add(flipPO);
                    //On ajoute le commentaire de mise à jour
                    ParseObject commentairePO = new ParseFactory().getParseObject(commentaireUpdate);
                    //Pointer sur le flip
                    commentairePO.put(FlipperDatabaseHandler.COMM_FLIP_POINTER, objects.get(0));
                    listParseToSave.add(commentairePO);

                    // Et on balance in da cloud!
                    ParseObject.saveAllInBackground(listParseToSave, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            mProgressBarHandler.hide();
                            if (e == null) {
                                Log.d(TAG, "SaveInBackground successful");
                                // Ca s'est bien passé, on sauvegarde les flippers
                                List<Flipper> listBaseToSave = new ArrayList<Flipper>();
                                List<Commentaire> listBaseToSaveComment = new ArrayList<Commentaire>();
                                listBaseToSave.add(flipper);
                                listBaseToSaveComment.add(commentaireUpdate);

                                BaseFlipperService baseFlipperService = new BaseFlipperService();
                                BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
                                baseFlipperService.majListeFlipper(listBaseToSave, pContext);
                                baseCommentaireService.majListeCommentaire(listBaseToSaveComment, pContext);
                                //TODO METTRE A JOUR LES COMMENTAIRES ASSOCIES.


                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupChangementFlipOK), Toast.LENGTH_SHORT);
                                toast.show();
                                if (mFragmentCallback != null) {
                                    mFragmentCallback.onTaskDone();
                                }
                            } else {
                                Log.d(TAG, "erreur : " + e.toString());
                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupRetraitFlipKO), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });

                }
            }
        });
        return true;
    }


    public boolean remplaceModeleFlipper(final Context pContext, final Flipper ancienflipper, final Flipper nouveauFlipper, final Commentaire commentaire) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID, ancienflipper.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    // On vérifie d'abord que le flipper n'était pas déjà désactivé, pour ne pas créer de doublons
                    if (!objects.get(0).getBoolean(FlipperDatabaseHandler.FLIPPER_ACTIF)) {
                        new AlertDialog.Builder(pContext).setTitle("Envoi impossible!").setMessage("Le modèle a déjà été modifié par un autre utilisateur. Mettez à jour votre base de flipper pour voir les dernières modifications.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                    } else {

                        // On met à jour l'ancien flipper avec l'état et la date de màj
                        objects.get(0).put(FlipperDatabaseHandler.FLIPPER_DATMAJ, ancienflipper.getDateMaj());
                        objects.get(0).put(FlipperDatabaseHandler.FLIPPER_ACTIF, false);

                        // On créé l'objet du nouveau flipper
                        ParseObject flipPO = new ParseObject(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
                        /*flipPO.put(FlipperDatabaseHandler.FLIPPER_ACTIF, true);
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_DATMAJ, nouveauFlipper.getDateMaj());
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, nouveauFlipper.getIdEnseigne());
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_ID, nouveauFlipper.getId());
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_MODELE, nouveauFlipper.getIdModele());
                        flipPO.put(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E, "");*/

                        //VarianteTEST
                        //TODO si ca marche enlever le code ci dessous et ci dessous
                        ParseFactory parseFactory = new ParseFactory();
                        flipPO = parseFactory.getPOWithPointersToExistingObjects(nouveauFlipper);

                        // On met le tout dans une liste
                        List<ParseObject> listParseToSave = new ArrayList<ParseObject>();
                        listParseToSave.add(flipPO);
                        listParseToSave.add(objects.get(0));

                        // On met éventuellement le nouveau commentaire
                        if (commentaire != null) {
                            commentaire.setFlipper(nouveauFlipper);
                            ParseObject commentairePO = parseFactory.getParseObject(commentaire);
                            //Pointer sur le nouveau flip
                            commentairePO.put(FlipperDatabaseHandler.COMM_FLIP_POINTER, flipPO);

                            //OLD
                            /*ParseObject CommentairePO = new ParseObject(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME);
                            CommentairePO.put(FlipperDatabaseHandler.COMM_ACTIF, true);
                            CommentairePO.put(FlipperDatabaseHandler.COMM_DATE, commentaire.getDate());
                            CommentairePO.put(FlipperDatabaseHandler.COMM_FLIPPER_ID, commentaire.getFlipperId());
                            CommentairePO.put(FlipperDatabaseHandler.COMM_ID, commentaire.getId());
                            CommentairePO.put(FlipperDatabaseHandler.COMM_PSEUDO, commentaire.getPseudo());
                            CommentairePO.put(FlipperDatabaseHandler.COMM_TEXTE, commentaire.getTexte());*/

                            listParseToSave.add(commentairePO);
                        }


                        // Et on balance in da cloud!
                        ParseObject.saveAllInBackground(listParseToSave, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mProgressBarHandler.hide();
                                    // Ca s'est bien passé, on sauvegarde les flippers
                                    List<Flipper> listBaseToSave = new ArrayList<Flipper>();
                                    listBaseToSave.add(nouveauFlipper);
                                    listBaseToSave.add(ancienflipper);

                                    BaseFlipperService baseFlipperService = new BaseFlipperService();
                                    baseFlipperService.majListeFlipper(listBaseToSave, pContext);

                                    // Et éventuellement le commentaire
                                    if (commentaire != null) {
                                        BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
                                        baseCommentaireService.addCommentaire(commentaire, pContext);
                                    }
                                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupChangementModeleOK), Toast.LENGTH_SHORT);
                                    toast.show();
                                    if (mFragmentCallback != null) {
                                        mFragmentCallback.onTaskDone();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.popupChangementModeleKO), Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    }
                }
            }
        });

        return true;
    }

    public boolean ajouterFlipper(final Context pContext, Flipper flipper, Commentaire commentaire) {

        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();

        ParseFactory parseFactory = new ParseFactory();
        ArrayList<ParseObject> objectsToSend;

        // On créé l'objet du nouveau flipper et on l'ajoute à la liste d'envoi
        //TODO Cette methode juste pour les ajouts de flips dans des enseignes existantes, A ne pas utiliser pour creer de nouveaux flips

        objectsToSend = parseFactory.getPOWithPointersToExistingObjects(flipper, commentaire);

        ParseObject.saveAllInBackground(objectsToSend, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mProgressBarHandler.hide();
                if (e == null) {
                    Toast toast = Toast.makeText(pContext, "Envoi effectué, Merci pour votre contribution :)", Toast.LENGTH_LONG);
                    toast.show();
                    // Pour quitter l'activité:
                    // if (mFragmentCallback != null) {
                    //    mFragmentCallback.onTaskDone();
                } else {

                    Toast toast = Toast.makeText(pContext, "L'ajout d'un flipper a échoué.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        return true;
    }
}
