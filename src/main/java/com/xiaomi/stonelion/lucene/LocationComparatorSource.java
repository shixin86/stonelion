
package com.xiaomi.stonelion.lucene;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import xiaomi.com.stonelion.lucene.Util.AllInOneUserDocKey;

import java.io.IOException;

/**
 * @author shixin
 * @date 2012-7-2
 */
public class LocationComparatorSource extends FieldComparatorSource {
    private static final long serialVersionUID = -8213876185422676196L;

    private double latitude;
    private double longitude;

    public LocationComparatorSource(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public FieldComparator newComparator(String fieldName, int numHits, int sortPos, boolean reversed) throws IOException {
        return new LocationFieldComparator(numHits);
    }

    private class LocationFieldComparator extends FieldComparator {
        private double[] distances, latitudes, longitudes;
        private double bottom;

        public LocationFieldComparator(int numHits) {
            distances = new double[numHits];
        }

        @Override
        public void setNextReader(IndexReader indexReader, int docBase) throws IOException {
            latitudes = FieldCache.DEFAULT.getDoubles(indexReader, AllInOneUserDocKey.LATITUDE);
            longitudes = FieldCache.DEFAULT.getDoubles(indexReader, AllInOneUserDocKey.LONGITUDE);
        }

        @Override
        public int compare(int slot1, int slot2) {
            if (distances[slot1] < distances[slot2])
                return -1;
            if (distances[slot1] > distances[slot2])
                return 1;
            return 0;
        }

        @Override
        public int compareBottom(int doc) throws IOException {
            double dosDistance = LocationBasedUtil.getDistance(longitude, latitude, longitudes[doc], latitudes[doc]);
            if (bottom < dosDistance)
                return -1;
            if (bottom > dosDistance)
                return 1;
            return 0;
        }

        @Override
        public void copy(int slot, int doc) throws IOException {
            distances[slot] = LocationBasedUtil.getDistance(longitude, latitude, longitudes[doc], latitudes[doc]);
        }

        @Override
        public void setBottom(int slot) {
            this.bottom = distances[slot];
        }

        @Override
        public Comparable<Double> value(int slot) {
            return new Double(distances[slot]);
        }
    }
}
