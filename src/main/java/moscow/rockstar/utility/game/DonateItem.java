package moscow.rockstar.utility.game;

import lombok.Generated;

public enum DonateItem {
   TAL_KRUSH("[★] Талисман Крушителя", new String[]{"tal-krush", "attribute-item-tkryshitela"}, 1, 15),
   TAL_KARATEL("[★] Талисман Карателя", new String[]{"tal-karatelya", "attribute-item-tkaratela"}, 3, 14),
   TAL_GARMONIA("[★] Талисман Гармонии", new String[]{"tal-garmonii", "attribute-item-tgarmonii"}, 2, 12),
   TAL_DEDALA("[★] Талисман Дедала", new String[]{"tal-dedala", "attribute-item-tdedala"}, 4, 11),
   TAL_EHIDNA("[★] Талисман Ехидна", new String[]{"tal-ehidna", "attribute-item-texidni"}, 6, 10),
   TAL_GRAN("[★] Талисман Грани", new String[]{"tal-grani", "attribute-item-tgrani"}, 5, 7),
   TAL_TRITON("[★] Талисман Тритона", new String[]{"tal-tritona", "attribute-item-ttritona"}, 8, 2),
   TAL_FENIX("[★] Талисман Феникс", new String[]{"tal-fenixa", "attribute-item-tfeniksa"}, 7, 1),
   SP_ANDROMEDA("[★] Сфера Андромеды", new String[]{"sphere-andromeda", "attribute-item-sandromedi"}, 8, 13),
   SP_PANDORA("[★] Сфера Пандоры", new String[]{"sphere-pandora", "attribute-item-spandori"}, 9, 9),
   SP_HIMERA("[★] Сфера Химеры", new String[]{"sphere-himeri", "attribute-item-shimeri"}, 10, 8),
   SP_TITAN("[★] Сфера Титана", new String[]{"sphere-titana", "attribute-item-stitana"}, 11, 6),
   SP_ASTREYA("[★] Сфера Астрея", new String[]{"sphere-astreya", "attribute-item-sastreya"}, 11, 5),
   SP_APOLLON("[★] Сфера Аполлона", new String[]{"sphere-apollona", "attribute-item-sapollona"}, 11, 4),
   SP_OSIRIS("[★] Сфера Осириса", new String[]{"sphere-osirisa", "attribute-item-sosirisa"}, 11, 3),
   TAL_INFINITY("Талисман ɪɴғɪɴɪᴛʏ", new String[]{"Infinity"}, 1, 1),
   TAL_ETERNITY("Талисман ᴇᴛᴇʀɴɪᴛʏ", new String[]{"Eternity"}, 2, 1),
   TAL_STINGER("Талисман sᴛɪɴɢᴇʀ", new String[]{"Stinger"}, 3, 1),
   TAL_SATIRI("Талисман Сатиры", new String[]{"Satir"}, 4, 1),
   TAL_MIF("Мифический талисман", new String[]{"MYTHICAL"}, 5, 1),
   TAL_LEG("Легендарный талисман", new String[]{"LEGENDARY"}, 6, 1),
   TAL_EPIC("Эпический талисман", new String[]{"EPIC"}, 7, 1),
   TAL_DEFAULT("Обычный талисман", new String[]{"NORMAL"}, 8, 1);

   private final String name;
   private final String[] nbt;
   private final int totem;
   private final int factor;

   private DonateItem(String name, String[] nbt, int totem, int factor) {
      this.name = name;
      this.nbt = nbt;
      this.totem = totem;
      this.factor = factor;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String[] getNbt() {
      return this.nbt;
   }

   @Generated
   public int getTotem() {
      return this.totem;
   }

   @Generated
   public int getFactor() {
      return this.factor;
   }
}
