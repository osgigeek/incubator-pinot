/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.query.selection;

import java.io.Serializable;
import org.apache.pinot.common.utils.DataSchema;
import org.apache.pinot.core.common.Block;
import org.apache.pinot.core.query.selection.iterator.BytesSelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.DoubleArraySelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.DoubleSelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.FloatArraySelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.FloatSelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.IntArraySelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.IntSelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.LongArraySelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.LongSelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.SelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.SelectionSingleValueColumnWithDictIterator;
import org.apache.pinot.core.query.selection.iterator.StringArraySelectionColumnIterator;
import org.apache.pinot.core.query.selection.iterator.StringSelectionColumnIterator;


/**
 * Selection fetcher is used for querying rows from given blocks and schema.
 * SelectionFetcher will initialize iterators on each data column and provide
 * the ability to return a Serializable array as a row for a given docId.
 *
 */
public class SelectionFetcher {
  private final int _numColumns;
  private final SelectionColumnIterator[] _selectionColumnIterators;

  public SelectionFetcher(Block[] blocks, DataSchema dataSchema) {
    _numColumns = blocks.length;
    _selectionColumnIterators = new SelectionColumnIterator[_numColumns];

    for (int i = 0; i < _numColumns; i++) {
      Block block = blocks[i];
      DataSchema.ColumnDataType columnDataType = dataSchema.getColumnDataType(i);
      if (block.getMetadata().hasDictionary()) {
        // With dictionary

        switch (columnDataType) {
          // Single value
          case INT:
          case LONG:
          case FLOAT:
          case DOUBLE:
          case STRING:
          case BYTES:
            _selectionColumnIterators[i] = new SelectionSingleValueColumnWithDictIterator(block);
            break;
          // Multi value
          case INT_ARRAY:
            _selectionColumnIterators[i] = new IntArraySelectionColumnIterator(block);
            break;
          case FLOAT_ARRAY:
            _selectionColumnIterators[i] = new FloatArraySelectionColumnIterator(block);
            break;
          case LONG_ARRAY:
            _selectionColumnIterators[i] = new LongArraySelectionColumnIterator(block);
            break;
          case DOUBLE_ARRAY:
            _selectionColumnIterators[i] = new DoubleArraySelectionColumnIterator(block);
            break;
          case STRING_ARRAY:
            _selectionColumnIterators[i] = new StringArraySelectionColumnIterator(block);
            break;
          default:
            throw new UnsupportedOperationException();
        }
      } else {
        // No dictionary

        switch (columnDataType) {
          case INT:
            _selectionColumnIterators[i] = new IntSelectionColumnIterator(block);
            break;
          case LONG:
            _selectionColumnIterators[i] = new LongSelectionColumnIterator(block);
            break;
          case FLOAT:
            _selectionColumnIterators[i] = new FloatSelectionColumnIterator(block);
            break;
          case DOUBLE:
            _selectionColumnIterators[i] = new DoubleSelectionColumnIterator(block);
            break;
          case STRING:
            _selectionColumnIterators[i] = new StringSelectionColumnIterator(block);
            break;
          case BYTES:
            _selectionColumnIterators[i] = new BytesSelectionColumnIterator(block);
            break;
          // TODO: add multi value support
          default:
            throw new UnsupportedOperationException();
        }
      }
    }
  }

  public Serializable[] getRow(int docId) {
    final Serializable[] row = new Serializable[_numColumns];
    for (int i = 0; i < _numColumns; i++) {
      row[i] = _selectionColumnIterators[i].getValue(docId);
    }
    return row;
  }
}
