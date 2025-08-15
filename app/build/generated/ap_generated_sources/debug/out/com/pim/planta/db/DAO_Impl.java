package com.pim.planta.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pim.planta.models.DiaryEntry;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DAO_Impl implements DAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DiaryEntry> __insertionAdapterOfDiaryEntry;

  private final EntityDeletionOrUpdateAdapter<DiaryEntry> __updateAdapterOfDiaryEntry;

  public DAO_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDiaryEntry = new EntityInsertionAdapter<DiaryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `diary_entries` (`id`,`highlight`,`annotation`,`emotion`,`userUid`,`date`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DiaryEntry entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getHighlight() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getHighlight());
        }
        if (entity.getAnnotation() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAnnotation());
        }
        statement.bindLong(4, entity.getEmotion());
        if (entity.getUserUid() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUserUid());
        }
        statement.bindLong(6, entity.getDate());
      }
    };
    this.__updateAdapterOfDiaryEntry = new EntityDeletionOrUpdateAdapter<DiaryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `diary_entries` SET `id` = ?,`highlight` = ?,`annotation` = ?,`emotion` = ?,`userUid` = ?,`date` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DiaryEntry entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getHighlight() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getHighlight());
        }
        if (entity.getAnnotation() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAnnotation());
        }
        statement.bindLong(4, entity.getEmotion());
        if (entity.getUserUid() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUserUid());
        }
        statement.bindLong(6, entity.getDate());
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public void insertDiaryEntry(final DiaryEntry entry) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDiaryEntry.insert(entry);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateDiaryEntry(final DiaryEntry entry) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfDiaryEntry.handle(entry);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public DiaryEntry getDiaryEntryByUserAndDate(final String userUid, final long date) {
    final String _sql = "SELECT * FROM diary_entries WHERE userUid = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userUid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userUid);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, date);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHighlight = CursorUtil.getColumnIndexOrThrow(_cursor, "highlight");
      final int _cursorIndexOfAnnotation = CursorUtil.getColumnIndexOrThrow(_cursor, "annotation");
      final int _cursorIndexOfEmotion = CursorUtil.getColumnIndexOrThrow(_cursor, "emotion");
      final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final DiaryEntry _result;
      if (_cursor.moveToFirst()) {
        final String _tmpHighlight;
        if (_cursor.isNull(_cursorIndexOfHighlight)) {
          _tmpHighlight = null;
        } else {
          _tmpHighlight = _cursor.getString(_cursorIndexOfHighlight);
        }
        final String _tmpAnnotation;
        if (_cursor.isNull(_cursorIndexOfAnnotation)) {
          _tmpAnnotation = null;
        } else {
          _tmpAnnotation = _cursor.getString(_cursorIndexOfAnnotation);
        }
        final int _tmpEmotion;
        _tmpEmotion = _cursor.getInt(_cursorIndexOfEmotion);
        final String _tmpUserUid;
        if (_cursor.isNull(_cursorIndexOfUserUid)) {
          _tmpUserUid = null;
        } else {
          _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
        }
        final long _tmpDate;
        _tmpDate = _cursor.getLong(_cursorIndexOfDate);
        _result = new DiaryEntry(_tmpHighlight,_tmpAnnotation,_tmpEmotion,_tmpUserUid,_tmpDate);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DiaryEntry> getEntradasByUserUid(final String userUid) {
    final String _sql = "SELECT * FROM diary_entries WHERE userUid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userUid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userUid);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHighlight = CursorUtil.getColumnIndexOrThrow(_cursor, "highlight");
      final int _cursorIndexOfAnnotation = CursorUtil.getColumnIndexOrThrow(_cursor, "annotation");
      final int _cursorIndexOfEmotion = CursorUtil.getColumnIndexOrThrow(_cursor, "emotion");
      final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final List<DiaryEntry> _result = new ArrayList<DiaryEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DiaryEntry _item;
        final String _tmpHighlight;
        if (_cursor.isNull(_cursorIndexOfHighlight)) {
          _tmpHighlight = null;
        } else {
          _tmpHighlight = _cursor.getString(_cursorIndexOfHighlight);
        }
        final String _tmpAnnotation;
        if (_cursor.isNull(_cursorIndexOfAnnotation)) {
          _tmpAnnotation = null;
        } else {
          _tmpAnnotation = _cursor.getString(_cursorIndexOfAnnotation);
        }
        final int _tmpEmotion;
        _tmpEmotion = _cursor.getInt(_cursorIndexOfEmotion);
        final String _tmpUserUid;
        if (_cursor.isNull(_cursorIndexOfUserUid)) {
          _tmpUserUid = null;
        } else {
          _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
        }
        final long _tmpDate;
        _tmpDate = _cursor.getLong(_cursorIndexOfDate);
        _item = new DiaryEntry(_tmpHighlight,_tmpAnnotation,_tmpEmotion,_tmpUserUid,_tmpDate);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getEmotionByUserAndDate(final String userUid, final long date) {
    final String _sql = "SELECT emotion FROM diary_entries WHERE userUid = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userUid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userUid);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, date);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public String getNoteByUserAndDate(final String userUid, final long date) {
    final String _sql = "SELECT annotation FROM diary_entries WHERE userUid = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userUid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userUid);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, date);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final String _result;
      if (_cursor.moveToFirst()) {
        if (_cursor.isNull(0)) {
          _result = null;
        } else {
          _result = _cursor.getString(0);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public String getHighlightByUserAndDate(final String userUid, final long date) {
    final String _sql = "SELECT highlight FROM diary_entries WHERE userUid = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userUid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userUid);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, date);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final String _result;
      if (_cursor.moveToFirst()) {
        if (_cursor.isNull(0)) {
          _result = null;
        } else {
          _result = _cursor.getString(0);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
