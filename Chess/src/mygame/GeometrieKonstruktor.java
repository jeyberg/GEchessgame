/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import daten.D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.Display;

/**
 * Übernimmt Manipulation und Erstellung von 3D Objekten.
 *
 * @author ivan
 */
public class GeometrieKonstruktor {

    String gewaehleteKachel;
    private String skinPfadFigur = "Models/Marmor/";
    private String skinPfadSchwarz = "Models/Marmor/KachelSchwarz/kachelS.j3o";
    private String skinPfadWeiss = "Models/Marmor/KachelWeiss/kachelW.j3o";
    Map<String, String> markierteKacheln = new HashMap<>();
    private Map<String, Vector3f> positionen = new HashMap<>();
    private Map<String, Geometry> figurenPositionen = new HashMap<>();
    private Node geschlagenW = new Node("geschlagenW");
    private Node figurenW = new Node("figurenW");
    Node schachbrett = new Node("chessboard");
    private Node geschlagenS = new Node("geschlagenS");

    /**
     * Stellt die im Verlauf der Partie geschlagene Figuren dar. Schwarze
     * Figuren werden auf der Seite von Weiss dargestellt und umgekehrt. Die
     * Liste der Figuren wird nach Folgender Rangfolge sortiert: Dame > Turm >
     * Läufer > Springer > Bauer.
     *
     * @param geschlageneFiguren Liste von Geometry Objekten, die die
     * geschlagenen Figuren darstellen.
     * @param rootNode Das oberste Node Objekt. Siehe jMonkey Dokumentation für
     * mehr Info über Node.
     */
    public void zeigeGeschlageneFiguren(ArrayList<Geometry> geschlageneFiguren, Node rootNode) {
        Collections.sort(geschlageneFiguren, new Comparator<Geometry>() {
            public int compare(Geometry o1, Geometry o2) {
                String s = (String) o1.getUserData("rang");
                String s2 = (String) o2.getUserData("rang");
                return s.compareTo(s2);
            }
        });
        for (Geometry g : geschlageneFiguren) {
            String farbe = g.getUserData("farbe");
            int count = 0;
            float x = 0;
            float z = 0;
            float y = 0.0f;
            if (farbe.equals("weiss")) {
                geschlagenW.attachChild(g);
                count = geschlagenW.getQuantity();
                if (count > 8) {
                    z = -15.0F;
                    x = -25.0F + (2 * count);
                } else {
                    z = -13.0F;
                    x = -9.0F + (2 * count);
                }
            } else if (farbe.equals("schwarz")) {
                geschlagenS.attachChild(g);
                count = geschlagenS.getQuantity();
                if (count > 8) {
                    z = 15.0F;
                    x = 25.0F - (2 * count);
                } else {
                    z = 13.0F;
                    x = 9.0F - (2 * count);
                }
            }
            Vector3f position = new Vector3f(x, y, z);
            g.setLocalTranslation(position);
        }
        rootNode.attachChild(geschlagenW);
        rootNode.attachChild(geschlagenS);
    }

    /**
     * Stellt die Kacheln eines Schachbretts dar.
     *
     * @param assetManager Wird benötigt um den Shader für die Kacheln zu laden.
     * @param rootNode Das oberste Node Objekt. Siehe jMonkey Dokumentation für
     * mehr Info über Node.
     */
    public void initBrett(AssetManager assetManager, Node rootNode) {
        boolean lastFieldBlack = true;
        int x = -7;
        int y = -7;
        char letter = 'a';
        int number = 8;
        Vector3f pos;
        String name;

        /*Box box = new Box(1.0F, 0.01F, 1.0F);
         Material weiss = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         weiss.setColor("Color", ColorRGBA.White);
         Material schwarz = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         schwarz.setColor("Color", ColorRGBA.DarkGray);*/
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                name = letter + "" + number;
                Geometry g;
                if (lastFieldBlack) {
                    Spatial geom = assetManager.loadModel(skinPfadWeiss); // "Models/chessboard_white_tile/chessboard_white_tile.j3o"
                    g = getGeometryFromSpatial(geom);
                    lastFieldBlack = false;
                    g.setUserData("farbe", "weiss");
                } else {
                    Spatial geom = assetManager.loadModel(skinPfadSchwarz); // "Models/chessboard_black_tile/chessboard_black_tile.j3o"
                    g = getGeometryFromSpatial(geom);
                    lastFieldBlack = true;
                    g.setUserData("farbe", "schwarz");
                }
                pos = new Vector3f(x, -1f, y);
                g.setLocalTranslation(pos);
                g.setUserData("typ", "kachel");
                g.setUserData("markiert", false);
                g.setUserData("position", name);
                g.setName(name);
                x += 2;
                letter++;
                schachbrett.attachChild(g);
                positionen.put(name, pos);
            }
            y += 2;
            x = -7;
            lastFieldBlack = !lastFieldBlack;
            number--;
            letter = 'a';
        }
        rootNode.attachChild(schachbrett);
    }

    /**
     * Holt das Geometry Objekt aus dem übergebenem Spatial. Das erlaubt ein
     * einfacheres Handeln der Figuren.
     *
     * @param spatial
     * @return das Geometry Objekt im Spatial
     */
    private Geometry getGeometryFromSpatial(Spatial spatial) {
        if (spatial instanceof Geometry) {
            return (Geometry) spatial;
        }
        Node node = (Node) spatial;
        return getGeometryFromSpatial(node.getChild(0));
    }

    /**
     * Initialisiert die Anzeige für das Fadenkreuz.
     *
     * @param guiFont Globales Objekt der Oberklasse von Main, zeigt auf das vom
     * assetManager geladene Font.
     * @param main Referenz auf das Main Objekt, das die Methode aufruft.
     * @param assetManager Objekt zum Laden von Assets wie Bilder, Shader,
     * Fonts.
     * @return Ein BitmapText Objekt, welches das "+" enthält.
     */
    public BitmapText initFadenkreuz(BitmapFont guiFont, Main main, AssetManager assetManager) {
        main.setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");
        ch.setLocalTranslation(Display.getWidth() / 2 - ch.getLineWidth() / 2, Display.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        return ch;
    }

    /**
     * Erstellt ein Geometry Objekt basierend auf dem type Parameter und gibt es
     * zurück. Im Objekt werden noch zusätzliche Daten gespeichert: yOffset für
     * die korrekte Darstellung auf dem Brett und "rang" für die Sortierung in
     * zeigeGeschlageneFigur.
     *
     * @param type Typ der darzustellenden Figur, also Dame, Turm, etc.
     * @return Ein Geometry Objekt mit entsprechenden Daten und Form.
     */
    public Geometry getGeometry(String type, AssetManager assetManager, boolean istWeiss) {
        Geometry g = null;
        if (type.equals("Turm")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "TurmWeiss/turmW.j3o"));
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "TurmSchwarz/turmS.j3o"));
            }
            g.setUserData("rang", "d");

        } else if (type.equals("Springer")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "SpringerWeiss/pferdW.j3o"));
                g.rotate(0f, 1.5708f, 0f);
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "SpringerSchwarz/pferdS.j3o"));
                g.rotate(0f, -1.5708f, 0f);
            }
            g.setUserData("rang", "b");
        } else if (type.equals("Laeufer")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "LaeuferWeiss/laeuferW.j3o"));
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "LaeuferSchwarz/laeuferS.j3o"));
            }
            g.setUserData("rang", "c");
        } else if (type.equals("Koenig")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "KoenigWeiss/koenigW.j3o"));
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "KoenigSchwarz/koenigS.j3o"));
            }
            g.setUserData("rang", "f");
        } else if (type.equals("Dame")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "DameWeiss/dameW.j3o"));
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "DameSchwarz/dameS.j3o"));
            }
            g.setUserData("rang", "e");
        } else if (type.equals("Bauer")) {
            if (istWeiss) {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "BauerWeiss/bauerW.j3o"));
            } else {
                g = getGeometryFromSpatial(assetManager.loadModel(skinPfadFigur + "BauerSchwarz/bauerS.j3o"));
            }
            g.setUserData("rang", "a");
        }
        g.setLocalScale(0.1f, 0.1f, 0.1f);
        return g;
    }

    /**
     * Markiert Kacheln, um die möglichen Züge einer Figur darzustellen.
     *
     * @param plist Liste der Koordinaten der Kacheln, die markiert werden
     * sollen.
     * @param assetManager Objekt zum Laden von Assets wie Bilder, Shader,
     * Fonts.
     */
    public void markiereKacheln(List<String> plist, AssetManager assetManager) {
        resetKacheln(assetManager);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        for (String s : plist) {
            Spatial sp = schachbrett.getChild(s);
            markierteKacheln.put(s, (String) sp.getUserData("farbe"));
            sp.setMaterial(mat);
            sp.setUserData("markiert", true);
        }
    }

    /**
     * Setzt zuvor markierte Kacheln auf ihre ursprüngliche Farbe zurück.
     */
    public void resetKacheln(AssetManager assetManager) {
        if (!markierteKacheln.isEmpty()) {
            for (Map.Entry<String, String> entry : markierteKacheln.entrySet()) {
                String s = entry.getKey();
                String mat = entry.getValue();
                Geometry tile = null;
                if (mat.equals("weiss")) {
                    tile = getGeometryFromSpatial(assetManager.loadModel(skinPfadWeiss));
                } else if (mat.equals("schwarz")) {
                    tile = getGeometryFromSpatial(assetManager.loadModel(skinPfadSchwarz));
                }
                /*Geometry g = (Geometry) schachbrett.getChild(s);
                 g.setMaterial(mat);*/
                tile.setName(s);
                tile.setUserData("typ", "kachel");
                tile.setUserData("markiert", false);
                tile.setUserData("position", s);
                tile.setUserData("farbe", mat);
                Vector3f pos = positionen.get(s);
                pos.setY(-1f);
                tile.setLocalTranslation(pos);
                schachbrett.detachChildNamed(s);
                schachbrett.attachChild(tile);


            }
            markierteKacheln.clear();
        }
    }

    /**
     * Stellt die Ziffern am Rand des Bretts dar. Die Ziffern auf der linken
     * Seite des Bretts (von Schwarz / Weiss aus gesehen) sind dem Betrachter
     * zugewendet.
     *
     * @param guiFont Objekt zum Erstellen eines BitmapText Objeks
     * @param assetManager Objekt zum Laden von Assets wie Bilder, Shader,
     * Fonts.
     * @param rootNode Das oberste Node Objekt. Siehe jMonkey Dokumentation für
     * mehr Info über Node.
     */
    public void initRandZiffer(BitmapFont guiFont, AssetManager assetManager, Node rootNode) {
        float x = -9.0F;
        float z = -8.0F;
        int nummer = 8;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                BitmapText ch = new BitmapText(guiFont, false);
                ch.setSize(2.0F);
                ch.setText(String.valueOf(nummer));
                if (i < 1) {
                    ch.setLocalTranslation(x - 0.5F, 0, z);
                    ch.rotate(-1.5708F, 0, 0);
                } else {
                    ch.setLocalTranslation(x + 0.5F, 0, z + 2.5F);
                    ch.rotate(-1.5708F, 3.14159F, 0);
                }
                rootNode.attachChild(ch);
                z += 2;
                nummer--;
            }
            nummer = 8;
            x *= -1;
            z = -8.0F;
        }
    }

    /**
     * Initialisiert die positionen Hashmap. Key: Schabrettkoordinate in
     * Schachnotation. Value: Vector3f Objekt mit Koordinaten entsprechend dem
     * Key.
     */
    public void initPositionen() {
        char letter = 'a';
        int number = 8;
        int x = -7;
        int y = -7;
        Vector3f pos;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pos = new Vector3f(x, 0.0F, y);
                positionen.put(letter + "" + number, pos);
                x += 2;
                letter++;
            }
            y += 2;
            x = -7;
            number--;
            letter = 'a';
        }
    }

    /**
     * Stellt die Buchstaben am Rand des Bretts dar. Die Buchstaben auf der
     * linken Seite des Bretts (von Schwarz / Weiss aus gesehen) sind dem
     * Betrachter zugewendet.
     *
     * @param guiFont Objekt zum Erstellen eines BitmapText Objeks
     * @param assetManager Objekt zum Laden von Assets wie Bilder, Shader,
     * Fonts.
     * @param rootNode Das oberste Node Objekt. Siehe jMonkey Dokumentation für
     * mehr Info über Node.
     */
    public void initRandBuchstabe(BitmapFont guiFont, AssetManager assetManager, Node rootNode) {
        float x = -7.0F;
        float z = -10.5F;
        char buchstabe = 'a';
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                BitmapText ch = new BitmapText(guiFont, false);
                ch.setSize(2.0F);
                ch.setText(String.valueOf(buchstabe));
                if (i > 0) {
                    ch.setLocalTranslation(x - 0.5F, 0, z);
                    ch.rotate(-1.5708F, 0, 0);
                } else {
                    ch.setLocalTranslation(x + 0.5F, 0, z + 2.5F);
                    ch.rotate(-1.5708F, 3.14159F, 0);
                }
                rootNode.attachChild(ch);
                x += 2;
                buchstabe++;
            }
            buchstabe = 'a';
            x = -7;
            z = 8;
        }
    }

    /**
     * Stellt die Figuren, die eine Position besitzen, auf dem Brett dar.
     * Zusätzliche Daten werden in den Geometry Objekten gespeichert: "farbe".
     * Um eine Überschneidung der Figuren mit dem Brett zu vermeiden wird die
     * zuvor in getGeometry gesetzte Eigenschaft "yOffset" benutzt. Figuren, die
     * über keine Position verfügen, werden an zeigeGeschlageneFigur
     * weitergegeben.
     *
     * @param xml String mit Daten über die Figuren im XML Format.
     * @param assetManager Objekt zum Laden von Assets wie Bilder, Shader,
     * Fonts.
     * @param rootNode Das oberste Node Objekt. Siehe jMonkey Dokumentation für
     * mehr Info über Node.
     */
    public void figuren(String xml, AssetManager assetManager, Node rootNode) {
        ArrayList<D> data = Xml.toArray(xml);
        ArrayList<Geometry> geschlageneFiguren = new ArrayList<Geometry>();
        if (!data.isEmpty()) {
            schachbrett.detachAllChildren();
            geschlagenW.detachAllChildren();
            geschlagenS.detachAllChildren();
            initBrett(assetManager, rootNode);
            for (D d : data) {
                if (d.getProperties().getProperty("klasse").equals("D_Figur")) {
                    Geometry g = null;
                    if (d.getProperties().getProperty("isWeiss").equals("true")) {
                        g = getGeometry(d.getProperties().getProperty("typ"), assetManager, true);
                        g.setUserData("farbe", "weiss");
                    } else {
                        g = getGeometry(d.getProperties().getProperty("typ"), assetManager, false);
                        g.setUserData("farbe", "schwarz");
                    }
                    if (!d.getProperties().getProperty("position").equals("")) {
                        String pos = d.getProperties().getProperty("position");
                        g.setUserData("position", d.getProperties().getProperty("position"));
                        figurenPositionen.put(pos, g);
                        Vector3f position = positionen.get(g.getUserData("position"));
                        position.setY(0.0f);
                        g.setLocalTranslation(position);
                        g.setUserData("typ", "figur");
                        schachbrett.attachChild(g);
                    } else {
                        geschlageneFiguren.add(g);
                    }
                }
            }
            if (!geschlageneFiguren.isEmpty()) {
                zeigeGeschlageneFiguren(geschlageneFiguren, rootNode);
            }
            rootNode.attachChild(figurenW);
            //rootNode.attachChild(figurenS);
        }
    }

    void setSkinPfad(String selectedId) {
        if (selectedId.equals("marmor")) {
            skinPfadFigur = "Models/Marmor/";
            skinPfadSchwarz = "Models/Marmor/KachelSchwarz/kachelS.j3o";
            skinPfadWeiss = "Models/Marmor/KachelWeiss/kachelW.j3o";
        } else if (selectedId.equals("holz_alt")) {
            skinPfadFigur = "Models/HolzAlt/";
            skinPfadSchwarz = "Models/HolzAlt/KachelSchwarz/kachelS.j3o";
            skinPfadWeiss = "Models/HolzAlt/KachelWeiss/kachelW.j3o";
        } else if (selectedId.equals("holz_jung")) {
            skinPfadFigur = "Models/HolzJung/";
            skinPfadSchwarz = "Models/HolzJung/KachelSchwarz/kachelS.j3o";
            skinPfadWeiss = "Models/HolzJung/KachelWeiss/kachelW.j3o";
        } else if (selectedId.equals("glas")) {
            skinPfadFigur = "Models/Glas/";
            skinPfadSchwarz = "Models/Glas/KachelSchwarz/kachelS.j3o";
            skinPfadWeiss = "Models/Glas/KachelWeiss/kachelW.j3o";
        }
    }

    public void aktualisiereFiguren(String xml, Node rootNode, AssetManager assetManager) {
        ArrayList<Geometry> geschlageneFiguren = new ArrayList<Geometry>();
        ArrayList<D> aktuelleBelegung = Xml.toArray(xml);
        String von = (String) aktuelleBelegung.get(0).getProperties().getProperty("von");
        String nach = (String) aktuelleBelegung.get(0).getProperties().getProperty("nach");
        String bemerkung = (String) aktuelleBelegung.get(0).getProperties().getProperty("bemerkung");
        
        Geometry gVon = figurenPositionen.get(von);

        if (figurenPositionen.containsKey(nach) && figurenPositionen.containsKey(von)) {
            geschlageneFiguren.add(figurenPositionen.get(nach));
            if (!geschlageneFiguren.isEmpty()) {
                zeigeGeschlageneFiguren(geschlageneFiguren, rootNode);
            }
            figurenPositionen.remove(nach);
        }
        
        if(bemerkung.equals("BauerUmgewandelt")){
            schachbrett.detachChild(gVon);
            if(gVon.getUserData("farbe").equals("weiss")){
                gVon = getGeometry("Dame", assetManager, true);
                gVon.setUserData("farbe", "weiss");
            }else {
                gVon = getGeometry("Dame", assetManager, false);
                gVon.setUserData("farbe", "schwarz");
            }
            gVon.setUserData("typ", "figur");
            gVon.setUserData("rang", "e");
            schachbrett.attachChild(gVon);
        }
        
        if(bemerkung.equals("RochadeLang") || bemerkung.equals("RochadeKurz")){
            Vector3f pos = null;
            Geometry turm = null;
            boolean rochadeKurz = bemerkung.equals("RochadeKurz");
            boolean rochadeLang = bemerkung.equals("RochadeLang");
            boolean isWeiss = false;
            String turmVon = null;
            String turmNach = null;
            
            if(gVon.getUserData("farbe").equals("weiss")) isWeiss = true;
            
            if(rochadeLang && isWeiss){
                turmVon = "a1";
                turmNach = "d1";
                turm = figurenPositionen.get("a1");
                pos = positionen.get(turmNach);
                pos.setY(0f);
            }else if(rochadeLang && !isWeiss){
                turmVon = "a8";
                turmNach = "d8";
                turm = figurenPositionen.get("a8");
                pos = positionen.get(turmNach);
                pos.setY(0f);
            }else if(rochadeKurz && isWeiss){
                turmVon = "h1";
                turmNach = "f1";
                turm = figurenPositionen.get("h1");
                pos = positionen.get(turmNach);
                pos.setY(0f);
            }else if(rochadeKurz && !isWeiss){
                turmVon = "h8";
                turmNach = "f8";
                turm = figurenPositionen.get("h8");
                pos = positionen.get(turmNach);
                pos.setY(0f);
            }
            turm.setUserData("position", turmNach);
            figurenPositionen.remove(turmVon);
            figurenPositionen.put(turmNach, turm);
            turm.setLocalTranslation(pos);
        }

        if (figurenPositionen.containsKey(von)) {
            if (gVon.getUserData("typ").equals("figur")) {
                Vector3f pos = positionen.get(nach);
                pos.setY(0f);
                gVon.setLocalTranslation(pos);
                gVon.setUserData("position", nach);
                figurenPositionen.remove(von);
                figurenPositionen.put(nach, gVon);
            }
        }
    }
}
