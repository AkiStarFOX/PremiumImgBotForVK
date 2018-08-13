/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: Mathias Lux, mathias@juggle.at
 * Date: 25.05.12
 * Time: 12:19
 */
public class Searcher {
    private String imgPath;
    private BufferedImage img = null;
    private boolean passed = false;
    private ArrayList<String> idList = new ArrayList<>();

    public Searcher(String imgPath) throws IOException {
        this.imgPath = imgPath;

        if (imgPath.length() > 0) {
            File f = new File(imgPath);
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    passed = true;
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        if (!passed) {
            System.out.println("No image given as first argument.");
            System.out.println("Run \"Searcher <query image>\" to search for <query image>.");
            System.exit(1);
        }

        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(Main.indexPath)));
        ImageSearcher searcher = new GenericFastImageSearcher(30, CEDD.class);
        // ImageSearcher searcher = new GenericFastImageSearcher(30, AutoColorCorrelogram.class); // for another image descriptor ...

        /*
            If you used DocValues while Indexing, use the following searcher:
         */
        // ImageSearcher searcher = new GenericDocValuesImageSearcher(30, CEDD.class, ir);

        // searching with a image file ...
        ImageSearchHits hits = searcher.search(img, ir);
        // searching with a Lucene document instance ...
        // ImageSearchHits hits = searcher.search(ir.document(0), ir);



        TreeMap<Double,Integer> map = new TreeMap<>();
        for (int i = 0; i < hits.length(); i++) {
            map.put(hits.score(i), hits.documentID(i));
        }

        for (Map.Entry m:map.entrySet()){
            String fileName = ir.document((int)m.getValue()).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(m.getKey() + ": \t" + fileName);
            File f = new File(fileName);
            idList.add(f.getName());
        }



    }

    public ArrayList<String> getIdList() {
        return idList;
    }
}




