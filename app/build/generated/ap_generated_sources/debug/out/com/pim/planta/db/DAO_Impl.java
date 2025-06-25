package com.pim.planta.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserPlantRelation;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DAO_Impl implements DAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Plant> __insertionAdapterOfPlant;

  private final EntityInsertionAdapter<DiaryEntry> __insertionAdapterOfDiaryEntry;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final EntityInsertionAdapter<UserPlantRelation> __insertionAdapterOfUserPlantRelation;

  private final EntityDeletionOrUpdateAdapter<Plant> __deletionAdapterOfPlant;

  private final EntityDeletionOrUpdateAdapter<DiaryEntry> __deletionAdapterOfDiaryEntry;

  private final EntityDeletionOrUpdateAdapter<User> __deletionAdapterOfUser;

  private final EntityDeletionOrUpdateAdapter<UserPlantRelation> __deletionAdapterOfUserPlantRelation;

  private final EntityDeletionOrUpdateAdapter<Plant> __updateAdapterOfPlant;

  private final EntityDeletionOrUpdateAdapter<DiaryEntry> __updateAdapterOfDiaryEntry;

  private final EntityDeletionOrUpdateAdapter<User> __updateAdapterOfUser;

  private final SharedSQLiteStatement __preparedStmtOfInsertUserPlantRelation;

  private final SharedSQLiteStatement __preparedStmtOfIncrementGrowCount;

  private final SharedSQLiteStatement __preparedStmtOfIncrementXpByPlantName;

  public DAO_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlant = new EntityInsertionAdapter<Plant>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `plants` (`id`,`name`,`basePath`,`imageResourceId`,`xp`,`xpMax`,`description`,`scientificName`,`nickname`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Plant entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getBasePath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBasePath());
        }
        statement.bindLong(4, entity.getImageResourceId());
        statement.bindLong(5, entity.getXp());
        statement.bindLong(6, entity.getXpMax());
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        if (entity.getScientificName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getScientificName());
        }
        if (entity.getNickname() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getNickname());
        }
      }
    };
    this.__insertionAdapterOfDiaryEntry = new EntityInsertionAdapter<DiaryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `diary-entries` (`id`,`highlight`,`annotation`,`emotion`,`user_id`,`date`) VALUES (nullif(?, 0),?,?,?,?,?)";
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
        statement.bindLong(5, entity.getUser_id());
        statement.bindLong(6, entity.getDate());
      }
    };
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `users` (`id`,`username`,`password`,`email`,`creationDate`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final User entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEmail());
        }
        statement.bindLong(5, entity.getCreationDate());
      }
    };
    this.__insertionAdapterOfUserPlantRelation = new EntityInsertionAdapter<UserPlantRelation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `user_plant_relation` (`userId`,`plantId`,`growCount`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final UserPlantRelation entity) {
        statement.bindLong(1, entity.userId);
        statement.bindLong(2, entity.plantId);
        statement.bindLong(3, entity.growCount);
      }
    };
    this.__deletionAdapterOfPlant = new EntityDeletionOrUpdateAdapter<Plant>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `plants` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Plant entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfDiaryEntry = new EntityDeletionOrUpdateAdapter<DiaryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `diary-entries` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DiaryEntry entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfUser = new EntityDeletionOrUpdateAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `users` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final User entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfUserPlantRelation = new EntityDeletionOrUpdateAdapter<UserPlantRelation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `user_plant_relation` WHERE `userId` = ? AND `plantId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final UserPlantRelation entity) {
        statement.bindLong(1, entity.userId);
        statement.bindLong(2, entity.plantId);
      }
    };
    this.__updateAdapterOfPlant = new EntityDeletionOrUpdateAdapter<Plant>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `plants` SET `id` = ?,`name` = ?,`basePath` = ?,`imageResourceId` = ?,`xp` = ?,`xpMax` = ?,`description` = ?,`scientificName` = ?,`nickname` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Plant entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getBasePath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBasePath());
        }
        statement.bindLong(4, entity.getImageResourceId());
        statement.bindLong(5, entity.getXp());
        statement.bindLong(6, entity.getXpMax());
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        if (entity.getScientificName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getScientificName());
        }
        if (entity.getNickname() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getNickname());
        }
        statement.bindLong(10, entity.getId());
      }
    };
    this.__updateAdapterOfDiaryEntry = new EntityDeletionOrUpdateAdapter<DiaryEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `diary-entries` SET `id` = ?,`highlight` = ?,`annotation` = ?,`emotion` = ?,`user_id` = ?,`date` = ? WHERE `id` = ?";
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
        statement.bindLong(5, entity.getUser_id());
        statement.bindLong(6, entity.getDate());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__updateAdapterOfUser = new EntityDeletionOrUpdateAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `id` = ?,`username` = ?,`password` = ?,`email` = ?,`creationDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final User entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEmail());
        }
        statement.bindLong(5, entity.getCreationDate());
        statement.bindLong(6, entity.getId());
      }
    };
    this.__preparedStmtOfInsertUserPlantRelation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT INTO user_plant_relation (userId, plantId) VALUES (?, ?)";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementGrowCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_plant_relation SET growCount = growCount + 1 WHERE userId = ? AND plantId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementXpByPlantName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE plants SET xp = xp + ? WHERE name = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final Plant planta) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPlant.insert(planta);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insert(final DiaryEntry entrada) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDiaryEntry.insert(entrada);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insert(final User usuario) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfUser.insert(usuario);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insert(final UserPlantRelation relation) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfUserPlantRelation.insert(relation);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Plant planta) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPlant.handle(planta);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final DiaryEntry entrada) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfDiaryEntry.handle(entrada);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final User usuario) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfUser.handle(usuario);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final UserPlantRelation relation) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfUserPlantRelation.handle(relation);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Plant planta) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfPlant.handle(planta);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final DiaryEntry entrada) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfDiaryEntry.handle(entrada);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final User usuario) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfUser.handle(usuario);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void commit(final Runnable operations) {
    __db.beginTransaction();
    try {
      DAO.super.commit(operations);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertUserPlantRelation(final int userId, final int plantId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfInsertUserPlantRelation.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, plantId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeInsert();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfInsertUserPlantRelation.release(_stmt);
    }
  }

  @Override
  public void incrementGrowCount(final int userId, final int plantId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementGrowCount.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, plantId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfIncrementGrowCount.release(_stmt);
    }
  }

  @Override
  public void incrementXpByPlantName(final String plantName, final int amount) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementXpByPlantName.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, amount);
    _argIndex = 2;
    if (plantName == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, plantName);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfIncrementXpByPlantName.release(_stmt);
    }
  }

  @Override
  public List<User> getAllUsuarios() {
    final String _sql = "SELECT * FROM users";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfCreationDate = CursorUtil.getColumnIndexOrThrow(_cursor, "creationDate");
      final List<User> _result = new ArrayList<User>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final User _item;
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _item = new User(_tmpUsername,_tmpEmail,_tmpPassword);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final long _tmpCreationDate;
        _tmpCreationDate = _cursor.getLong(_cursorIndexOfCreationDate);
        _item.setCreationDate(_tmpCreationDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Plant> getAllPlantas() {
    final String _sql = "SELECT * FROM plants";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfBasePath = CursorUtil.getColumnIndexOrThrow(_cursor, "basePath");
      final int _cursorIndexOfImageResourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "imageResourceId");
      final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
      final int _cursorIndexOfXpMax = CursorUtil.getColumnIndexOrThrow(_cursor, "xpMax");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfScientificName = CursorUtil.getColumnIndexOrThrow(_cursor, "scientificName");
      final int _cursorIndexOfNickname = CursorUtil.getColumnIndexOrThrow(_cursor, "nickname");
      final List<Plant> _result = new ArrayList<Plant>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Plant _item;
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpBasePath;
        if (_cursor.isNull(_cursorIndexOfBasePath)) {
          _tmpBasePath = null;
        } else {
          _tmpBasePath = _cursor.getString(_cursorIndexOfBasePath);
        }
        final int _tmpImageResourceId;
        _tmpImageResourceId = _cursor.getInt(_cursorIndexOfImageResourceId);
        final int _tmpXp;
        _tmpXp = _cursor.getInt(_cursorIndexOfXp);
        final int _tmpXpMax;
        _tmpXpMax = _cursor.getInt(_cursorIndexOfXpMax);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final String _tmpScientificName;
        if (_cursor.isNull(_cursorIndexOfScientificName)) {
          _tmpScientificName = null;
        } else {
          _tmpScientificName = _cursor.getString(_cursorIndexOfScientificName);
        }
        _item = new Plant(_tmpName,_tmpBasePath,_tmpImageResourceId,_tmpXp,_tmpXpMax,_tmpDescription,_tmpScientificName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpNickname;
        if (_cursor.isNull(_cursorIndexOfNickname)) {
          _tmpNickname = null;
        } else {
          _tmpNickname = _cursor.getString(_cursorIndexOfNickname);
        }
        _item.setNickname(_tmpNickname);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Plant getPlantaById(final int id) {
    final String _sql = "SELECT * FROM plants WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfBasePath = CursorUtil.getColumnIndexOrThrow(_cursor, "basePath");
      final int _cursorIndexOfImageResourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "imageResourceId");
      final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
      final int _cursorIndexOfXpMax = CursorUtil.getColumnIndexOrThrow(_cursor, "xpMax");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfScientificName = CursorUtil.getColumnIndexOrThrow(_cursor, "scientificName");
      final int _cursorIndexOfNickname = CursorUtil.getColumnIndexOrThrow(_cursor, "nickname");
      final Plant _result;
      if (_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpBasePath;
        if (_cursor.isNull(_cursorIndexOfBasePath)) {
          _tmpBasePath = null;
        } else {
          _tmpBasePath = _cursor.getString(_cursorIndexOfBasePath);
        }
        final int _tmpImageResourceId;
        _tmpImageResourceId = _cursor.getInt(_cursorIndexOfImageResourceId);
        final int _tmpXp;
        _tmpXp = _cursor.getInt(_cursorIndexOfXp);
        final int _tmpXpMax;
        _tmpXpMax = _cursor.getInt(_cursorIndexOfXpMax);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final String _tmpScientificName;
        if (_cursor.isNull(_cursorIndexOfScientificName)) {
          _tmpScientificName = null;
        } else {
          _tmpScientificName = _cursor.getString(_cursorIndexOfScientificName);
        }
        _result = new Plant(_tmpName,_tmpBasePath,_tmpImageResourceId,_tmpXp,_tmpXpMax,_tmpDescription,_tmpScientificName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpNickname;
        if (_cursor.isNull(_cursorIndexOfNickname)) {
          _tmpNickname = null;
        } else {
          _tmpNickname = _cursor.getString(_cursorIndexOfNickname);
        }
        _result.setNickname(_tmpNickname);
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
  public Plant getPlantaByName(final String name) {
    final String _sql = "SELECT * FROM plants WHERE name = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfBasePath = CursorUtil.getColumnIndexOrThrow(_cursor, "basePath");
      final int _cursorIndexOfImageResourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "imageResourceId");
      final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
      final int _cursorIndexOfXpMax = CursorUtil.getColumnIndexOrThrow(_cursor, "xpMax");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfScientificName = CursorUtil.getColumnIndexOrThrow(_cursor, "scientificName");
      final int _cursorIndexOfNickname = CursorUtil.getColumnIndexOrThrow(_cursor, "nickname");
      final Plant _result;
      if (_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpBasePath;
        if (_cursor.isNull(_cursorIndexOfBasePath)) {
          _tmpBasePath = null;
        } else {
          _tmpBasePath = _cursor.getString(_cursorIndexOfBasePath);
        }
        final int _tmpImageResourceId;
        _tmpImageResourceId = _cursor.getInt(_cursorIndexOfImageResourceId);
        final int _tmpXp;
        _tmpXp = _cursor.getInt(_cursorIndexOfXp);
        final int _tmpXpMax;
        _tmpXpMax = _cursor.getInt(_cursorIndexOfXpMax);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final String _tmpScientificName;
        if (_cursor.isNull(_cursorIndexOfScientificName)) {
          _tmpScientificName = null;
        } else {
          _tmpScientificName = _cursor.getString(_cursorIndexOfScientificName);
        }
        _result = new Plant(_tmpName,_tmpBasePath,_tmpImageResourceId,_tmpXp,_tmpXpMax,_tmpDescription,_tmpScientificName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpNickname;
        if (_cursor.isNull(_cursorIndexOfNickname)) {
          _tmpNickname = null;
        } else {
          _tmpNickname = _cursor.getString(_cursorIndexOfNickname);
        }
        _result.setNickname(_tmpNickname);
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
  public List<DiaryEntry> getEntradasByUserId(final int id) {
    final String _sql = "SELECT * FROM `diary-entries` WHERE user_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHighlight = CursorUtil.getColumnIndexOrThrow(_cursor, "highlight");
      final int _cursorIndexOfAnnotation = CursorUtil.getColumnIndexOrThrow(_cursor, "annotation");
      final int _cursorIndexOfEmotion = CursorUtil.getColumnIndexOrThrow(_cursor, "emotion");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
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
        final int _tmpUser_id;
        _tmpUser_id = _cursor.getInt(_cursorIndexOfUserId);
        final long _tmpDate;
        _tmpDate = _cursor.getLong(_cursorIndexOfDate);
        _item = new DiaryEntry(_tmpHighlight,_tmpAnnotation,_tmpEmotion,_tmpUser_id,_tmpDate);
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
  public DiaryEntry getEntradaByUserIdAndDate(final int userId, final long date) {
    final String _sql = "SELECT * FROM `diary-entries` WHERE user_id = ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, date);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHighlight = CursorUtil.getColumnIndexOrThrow(_cursor, "highlight");
      final int _cursorIndexOfAnnotation = CursorUtil.getColumnIndexOrThrow(_cursor, "annotation");
      final int _cursorIndexOfEmotion = CursorUtil.getColumnIndexOrThrow(_cursor, "emotion");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
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
        final int _tmpUser_id;
        _tmpUser_id = _cursor.getInt(_cursorIndexOfUserId);
        final long _tmpDate;
        _tmpDate = _cursor.getLong(_cursorIndexOfDate);
        _result = new DiaryEntry(_tmpHighlight,_tmpAnnotation,_tmpEmotion,_tmpUser_id,_tmpDate);
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
  public User getUserByEmail(final String email) {
    final String _sql = "SELECT * FROM users WHERE email = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (email == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, email);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfCreationDate = CursorUtil.getColumnIndexOrThrow(_cursor, "creationDate");
      final User _result;
      if (_cursor.moveToFirst()) {
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _result = new User(_tmpUsername,_tmpEmail,_tmpPassword);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final long _tmpCreationDate;
        _tmpCreationDate = _cursor.getLong(_cursorIndexOfCreationDate);
        _result.setCreationDate(_tmpCreationDate);
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
  public List<UserPlantRelation> getUserPlantRelations(final int userId) {
    final String _sql = "SELECT * FROM user_plant_relation WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfPlantId = CursorUtil.getColumnIndexOrThrow(_cursor, "plantId");
      final int _cursorIndexOfGrowCount = CursorUtil.getColumnIndexOrThrow(_cursor, "growCount");
      final List<UserPlantRelation> _result = new ArrayList<UserPlantRelation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final UserPlantRelation _item;
        final int _tmpUserId;
        _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
        final int _tmpPlantId;
        _tmpPlantId = _cursor.getInt(_cursorIndexOfPlantId);
        _item = new UserPlantRelation(_tmpUserId,_tmpPlantId);
        _item.growCount = _cursor.getInt(_cursorIndexOfGrowCount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getGrowCount(final int userId, final int plantId) {
    final String _sql = "SELECT growCount FROM user_plant_relation WHERE userId = ? AND plantId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, plantId);
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
  public LiveData<Plant> getLivePlantaByName(final String plantName) {
    final String _sql = "SELECT * FROM plants WHERE name = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (plantName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, plantName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"plants"}, false, new Callable<Plant>() {
      @Override
      @Nullable
      public Plant call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBasePath = CursorUtil.getColumnIndexOrThrow(_cursor, "basePath");
          final int _cursorIndexOfImageResourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "imageResourceId");
          final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
          final int _cursorIndexOfXpMax = CursorUtil.getColumnIndexOrThrow(_cursor, "xpMax");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfScientificName = CursorUtil.getColumnIndexOrThrow(_cursor, "scientificName");
          final int _cursorIndexOfNickname = CursorUtil.getColumnIndexOrThrow(_cursor, "nickname");
          final Plant _result;
          if (_cursor.moveToFirst()) {
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpBasePath;
            if (_cursor.isNull(_cursorIndexOfBasePath)) {
              _tmpBasePath = null;
            } else {
              _tmpBasePath = _cursor.getString(_cursorIndexOfBasePath);
            }
            final int _tmpImageResourceId;
            _tmpImageResourceId = _cursor.getInt(_cursorIndexOfImageResourceId);
            final int _tmpXp;
            _tmpXp = _cursor.getInt(_cursorIndexOfXp);
            final int _tmpXpMax;
            _tmpXpMax = _cursor.getInt(_cursorIndexOfXpMax);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpScientificName;
            if (_cursor.isNull(_cursorIndexOfScientificName)) {
              _tmpScientificName = null;
            } else {
              _tmpScientificName = _cursor.getString(_cursorIndexOfScientificName);
            }
            _result = new Plant(_tmpName,_tmpBasePath,_tmpImageResourceId,_tmpXp,_tmpXpMax,_tmpDescription,_tmpScientificName);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _result.setId(_tmpId);
            final String _tmpNickname;
            if (_cursor.isNull(_cursorIndexOfNickname)) {
              _tmpNickname = null;
            } else {
              _tmpNickname = _cursor.getString(_cursorIndexOfNickname);
            }
            _result.setNickname(_tmpNickname);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
