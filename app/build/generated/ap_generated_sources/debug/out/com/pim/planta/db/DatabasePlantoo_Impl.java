package com.pim.planta.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class DatabasePlantoo_Impl extends DatabasePlantoo {
  private volatile DAO _dAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(11) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `plants` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `basePath` TEXT, `imageResourceId` INTEGER NOT NULL, `xp` INTEGER NOT NULL, `xpMax` INTEGER NOT NULL, `description` TEXT, `scientificName` TEXT, `nickname` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `password` TEXT, `email` TEXT, `creationDate` INTEGER)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `calendar` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fechas` TEXT, `anotaciones` TEXT, `emociones` INTEGER NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `diary-entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `highlight` TEXT, `annotation` TEXT, `emotion` INTEGER NOT NULL, `user_id` INTEGER NOT NULL, `date` INTEGER NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `user_plant_relation` (`userId` INTEGER NOT NULL, `plantId` INTEGER NOT NULL, `growCount` INTEGER NOT NULL, PRIMARY KEY(`userId`, `plantId`), FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`plantId`) REFERENCES `plants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_plant_relation_userId_plantId` ON `user_plant_relation` (`userId`, `plantId`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ca417fb782568b3314921d592cfc08b9')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `plants`");
        _db.execSQL("DROP TABLE IF EXISTS `users`");
        _db.execSQL("DROP TABLE IF EXISTS `calendar`");
        _db.execSQL("DROP TABLE IF EXISTS `diary-entries`");
        _db.execSQL("DROP TABLE IF EXISTS `user_plant_relation`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        _db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsPlants = new HashMap<String, TableInfo.Column>(9);
        _columnsPlants.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("basePath", new TableInfo.Column("basePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("imageResourceId", new TableInfo.Column("imageResourceId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("xp", new TableInfo.Column("xp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("xpMax", new TableInfo.Column("xpMax", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("scientificName", new TableInfo.Column("scientificName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlants.put("nickname", new TableInfo.Column("nickname", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlants = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlants = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlants = new TableInfo("plants", _columnsPlants, _foreignKeysPlants, _indicesPlants);
        final TableInfo _existingPlants = TableInfo.read(_db, "plants");
        if (! _infoPlants.equals(_existingPlants)) {
          return new RoomOpenHelper.ValidationResult(false, "plants(com.pim.planta.models.Plant).\n"
                  + " Expected:\n" + _infoPlants + "\n"
                  + " Found:\n" + _existingPlants);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(5);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("creationDate", new TableInfo.Column("creationDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(_db, "users");
        if (! _infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.pim.planta.models.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsCalendar = new HashMap<String, TableInfo.Column>(4);
        _columnsCalendar.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendar.put("fechas", new TableInfo.Column("fechas", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendar.put("anotaciones", new TableInfo.Column("anotaciones", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendar.put("emociones", new TableInfo.Column("emociones", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCalendar = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCalendar = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCalendar = new TableInfo("calendar", _columnsCalendar, _foreignKeysCalendar, _indicesCalendar);
        final TableInfo _existingCalendar = TableInfo.read(_db, "calendar");
        if (! _infoCalendar.equals(_existingCalendar)) {
          return new RoomOpenHelper.ValidationResult(false, "calendar(com.pim.planta.models.Calendar).\n"
                  + " Expected:\n" + _infoCalendar + "\n"
                  + " Found:\n" + _existingCalendar);
        }
        final HashMap<String, TableInfo.Column> _columnsDiaryEntries = new HashMap<String, TableInfo.Column>(6);
        _columnsDiaryEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaryEntries.put("highlight", new TableInfo.Column("highlight", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaryEntries.put("annotation", new TableInfo.Column("annotation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaryEntries.put("emotion", new TableInfo.Column("emotion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaryEntries.put("user_id", new TableInfo.Column("user_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaryEntries.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDiaryEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDiaryEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDiaryEntries = new TableInfo("diary-entries", _columnsDiaryEntries, _foreignKeysDiaryEntries, _indicesDiaryEntries);
        final TableInfo _existingDiaryEntries = TableInfo.read(_db, "diary-entries");
        if (! _infoDiaryEntries.equals(_existingDiaryEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "diary-entries(com.pim.planta.models.DiaryEntry).\n"
                  + " Expected:\n" + _infoDiaryEntries + "\n"
                  + " Found:\n" + _existingDiaryEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsUserPlantRelation = new HashMap<String, TableInfo.Column>(3);
        _columnsUserPlantRelation.put("userId", new TableInfo.Column("userId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPlantRelation.put("plantId", new TableInfo.Column("plantId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPlantRelation.put("growCount", new TableInfo.Column("growCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserPlantRelation = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysUserPlantRelation.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION",Arrays.asList("userId"), Arrays.asList("id")));
        _foreignKeysUserPlantRelation.add(new TableInfo.ForeignKey("plants", "CASCADE", "NO ACTION",Arrays.asList("plantId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesUserPlantRelation = new HashSet<TableInfo.Index>(1);
        _indicesUserPlantRelation.add(new TableInfo.Index("index_user_plant_relation_userId_plantId", true, Arrays.asList("userId","plantId"), Arrays.asList("ASC","ASC")));
        final TableInfo _infoUserPlantRelation = new TableInfo("user_plant_relation", _columnsUserPlantRelation, _foreignKeysUserPlantRelation, _indicesUserPlantRelation);
        final TableInfo _existingUserPlantRelation = TableInfo.read(_db, "user_plant_relation");
        if (! _infoUserPlantRelation.equals(_existingUserPlantRelation)) {
          return new RoomOpenHelper.ValidationResult(false, "user_plant_relation(com.pim.planta.models.UserPlantRelation).\n"
                  + " Expected:\n" + _infoUserPlantRelation + "\n"
                  + " Found:\n" + _existingUserPlantRelation);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ca417fb782568b3314921d592cfc08b9", "97a39d3b95c9683753af696cb809d80c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "plants","users","calendar","diary-entries","user_plant_relation");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `plants`");
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `calendar`");
      _db.execSQL("DELETE FROM `diary-entries`");
      _db.execSQL("DELETE FROM `user_plant_relation`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DAO.class, DAO_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public DAO DAO() {
    if (_dAO != null) {
      return _dAO;
    } else {
      synchronized(this) {
        if(_dAO == null) {
          _dAO = new DAO_Impl(this);
        }
        return _dAO;
      }
    }
  }
}
