<template>
  <div class="relative">
    <!-- Emoji Button -->
    <button
      ref="triggerButton"
      @click="togglePicker"
      type="button"
      class="toolbar-btn"
      :title="title"
    >
      <span class="text-base">😀</span>
    </button>

    <!-- Emoji Picker Dropdown -->
    <div
      v-if="isOpen"
      ref="pickerDropdown"
      class="absolute z-50 bg-white rounded-lg shadow-xl border border-gray-200 w-80"
      :style="dropdownStyle"
    >
      <!-- Header -->
      <div class="flex items-center justify-between p-2 border-b border-gray-200">
        <span class="text-sm font-medium text-gray-700">表情</span>
        <button
          @click="closePicker"
          class="p-1 hover:bg-gray-100 rounded transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Recently Used Section -->
      <div v-if="recentEmojis.length > 0" class="p-2 border-b border-gray-100">
        <div class="text-xs text-gray-500 mb-2">最近使用</div>
        <div class="flex flex-wrap gap-1">
          <button
            v-for="emoji in recentEmojis"
            :key="'recent-' + emoji.char"
            @click="selectEmoji(emoji)"
            class="emoji-btn"
            :title="emoji.name"
          >
            {{ emoji.char }}
          </button>
        </div>
      </div>

      <!-- Category Tabs -->
      <div class="flex border-b border-gray-200 px-2 overflow-x-auto">
        <button
          v-for="category in categories"
          :key="category.id"
          @click="activeCategory = category.id"
          class="flex-shrink-0 px-3 py-2 text-sm transition-colors"
          :class="activeCategory === category.id
            ? 'text-primary-600 border-b-2 border-primary-600'
            : 'text-gray-500 hover:text-gray-700'"
          :title="category.name"
        >
          {{ category.icon }}
        </button>
      </div>

      <!-- Emoji Grid -->
      <div class="p-2 h-48 overflow-y-auto">
        <div class="flex flex-wrap gap-1">
          <button
            v-for="emoji in currentEmojis"
            :key="emoji.char"
            @click="selectEmoji(emoji)"
            class="emoji-btn"
            :title="emoji.name"
          >
            {{ emoji.char }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'

// Emoji data with categories
const EMOJI_DATA = {
  smileys: {
    name: '表情',
    icon: '😀',
    emojis: [
      { char: '😀', name: '开心' },
      { char: '😃', name: '大笑' },
      { char: '😄', name: '高兴' },
      { char: '😁', name: '笑眯眯' },
      { char: '😆', name: '大笑' },
      { char: '😅', name: '尴尬笑' },
      { char: '🤣', name: '笑哭' },
      { char: '😂', name: '笑出眼泪' },
      { char: '🙂', name: '微笑' },
      { char: '😊', name: '害羞笑' },
      { char: '😇', name: '天使' },
      { char: '🥰', name: '喜爱' },
      { char: '😍', name: '花痴' },
      { char: '🤩', name: '好崇拜' },
      { char: '😘', name: '飞吻' },
      { char: '😗', name: '亲亲' },
      { char: '😚', name: '亲一个' },
      { char: '😙', name: '微笑亲亲' },
      { char: '🥲', name: '含泪微笑' },
      { char: '😋', name: '好吃' },
      { char: '😛', name: '吐舌头' },
      { char: '😜', name: '调皮' },
      { char: '🤪', name: '疯狂' },
      { char: '😝', name: '眯眼吐舌' },
      { char: '🤑', name: '发财' },
      { char: '🤗', name: '拥抱' },
      { char: '🤭', name: '捂嘴笑' },
      { char: '🤫', name: '嘘' },
      { char: '🤔', name: '思考' },
      { char: '🤐', name: '闭嘴' },
      { char: '🤨', name: '挑眉' },
      { char: '😐', name: '无语' },
      { char: '😑', name: '冷漠' },
      { char: '😶', name: '沉默' },
      { char: '😏', name: '得意' },
      { char: '😒', name: '不爽' },
      { char: '🙄', name: '翻白眼' },
      { char: '😬', name: '龇牙' },
      { char: '😮‍💨', name: '叹气' },
      { char: '🤥', name: '说谎' },
      { char: '😌', name: '满足' },
      { char: '😔', name: '沮丧' },
      { char: '😪', name: '困了' },
      { char: '🤤', name: '流口水' },
      { char: '😴', name: '睡觉' },
      { char: '😷', name: '口罩' },
      { char: '🤒', name: '发烧' },
      { char: '🤕', name: '受伤' },
      { char: '🤢', name: '恶心' },
      { char: '🤮', name: '呕吐' },
      { char: '🤧', name: '打喷嚏' },
      { char: '🥵', name: '好热' },
      { char: '🥶', name: '好冷' },
      { char: '🥴', name: '醉了' },
      { char: '😵', name: '晕' },
      { char: '🤯', name: '爆炸' },
      { char: '🤠', name: '牛仔' },
      { char: '🥳', name: '庆祝' },
      { char: '🥸', name: '伪装' },
      { char: '😎', name: '墨镜' },
      { char: '🤓', name: '书呆子' },
      { char: '🧐', name: '单片眼镜' },
      { char: '😕', name: '困惑' },
      { char: '😟', name: '担心' },
      { char: '🙁', name: '不高兴' },
      { char: '😮', name: '惊讶' },
      { char: '😯', name: '沉默惊讶' },
      { char: '😲', name: '震惊' },
      { char: '😳', name: '脸红' },
      { char: '🥺', name: '可怜' },
      { char: '😦', name: '愁眉' },
      { char: '😧', name: '痛苦' },
      { char: '😨', name: '害怕' },
      { char: '😰', name: '焦虑' },
      { char: '😥', name: '失望' },
      { char: '😢', name: '哭泣' },
      { char: '😭', name: '大哭' },
      { char: '😱', name: '尖叫' },
      { char: '😖', name: '纠结' },
      { char: '😣', name: '忍耐' },
      { char: '😞', name: '失望' },
      { char: '😓', name: '冷汗' },
      { char: '😩', name: '疲惫' },
      { char: '😫', name: '累坏了' },
      { char: '🥱', name: '打哈欠' },
      { char: '😤', name: '生气' },
      { char: '😡', name: '愤怒' },
      { char: '😠', name: '不满' },
      { char: '🤬', name: '骂人' },
      { char: '😈', name: '小恶魔' },
      { char: '👿', name: '恶魔' },
      { char: '💀', name: '骷髅' },
      { char: '☠️', name: '骷髅头' },
      { char: '💩', name: '便便' },
      { char: '🤡', name: '小丑' },
      { char: '👹', name: '妖怪' },
      { char: '👺', name: '天狗' },
      { char: '👻', name: '幽灵' },
      { char: '👽', name: '外星人' },
      { char: '👾', name: '怪物' },
      { char: '🤖', name: '机器人' }
    ]
  },
  gestures: {
    name: '手势',
    icon: '👋',
    emojis: [
      { char: '👋', name: '挥手' },
      { char: '🤚', name: '手背' },
      { char: '🖐️', name: '张开手' },
      { char: '✋', name: '举手' },
      { char: '🖖', name: 'Vulcan手势' },
      { char: '👌', name: 'OK' },
      { char: '🤌', name: '捏手指' },
      { char: '🤏', name: '捏' },
      { char: '✌️', name: '胜利' },
      { char: '🤞', name: '交叉手指' },
      { char: '🤟', name: '爱你' },
      { char: '🤘', name: '摇滚' },
      { char: '🤙', name: '打电话' },
      { char: '👈', name: '向左指' },
      { char: '👉', name: '向右指' },
      { char: '👆', name: '向上指' },
      { char: '🖕', name: '中指' },
      { char: '👇', name: '向下指' },
      { char: '☝️', name: '食指向上' },
      { char: '👍', name: '点赞' },
      { char: '👎', name: '踩' },
      { char: '✊', name: '握拳' },
      { char: '👊', name: '出拳' },
      { char: '🤛', name: '左拳' },
      { char: '🤜', name: '右拳' },
      { char: '👏', name: '鼓掌' },
      { char: '🙌', name: '举双手' },
      { char: '👐', name: '张开双手' },
      { char: '🤲', name: '掌心向上' },
      { char: '🤝', name: '握手' },
      { char: '🙏', name: '合十' },
      { char: '✍️', name: '写字' },
      { char: '💪', name: '肌肉' },
      { char: '🦾', name: '机械臂' },
      { char: '🦿', name: '机械腿' },
      { char: '🦵', name: '腿' },
      { char: '🦶', name: '脚' },
      { char: '👂', name: '耳朵' },
      { char: '🦻', name: '助听器' },
      { char: '👃', name: '鼻子' },
      { char: '🧠', name: '大脑' },
      { char: '👀', name: '眼睛' },
      { char: '👁️', name: '眼睛' },
      { char: '👅', name: '舌头' },
      { char: '👄', name: '嘴唇' }
    ]
  },
  hearts: {
    name: '心形',
    icon: '❤️',
    emojis: [
      { char: '❤️', name: '红心' },
      { char: '🧡', name: '橙心' },
      { char: '💛', name: '黄心' },
      { char: '💚', name: '绿心' },
      { char: '💙', name: '蓝心' },
      { char: '💜', name: '紫心' },
      { char: '🖤', name: '黑心' },
      { char: '🤍', name: '白心' },
      { char: '🤎', name: '棕心' },
      { char: '💔', name: '心碎' },
      { char: '❣️', name: '心叹号' },
      { char: '💕', name: '双心' },
      { char: '💞', name: '旋转心' },
      { char: '💓', name: '跳动心' },
      { char: '💗', name: '成长心' },
      { char: '💖', name: '闪亮心' },
      { char: '💘', name: '丘比特心' },
      { char: '💝', name: '礼物心' },
      { char: '💟', name: '装饰心' },
      { char: '♥️', name: '红桃' },
      { char: '💌', name: '情书' },
      { char: '💋', name: '吻痕' },
      { char: '🫂', name: '拥抱' },
      { char: '💐', name: '花束' },
      { char: '🌹', name: '玫瑰' },
      { char: '🥀', name: '枯萎花' },
      { char: '🌺', name: '芙蓉' },
      { char: '🌸', name: '樱花' },
      { char: '💮', name: '白花' },
      { char: '🏵️', name: '玫瑰花结' },
      { char: '🌷', name: '郁金香' },
      { char: '🌻', name: '向日葵' },
      { char: '🌼', name: '雏菊' }
    ]
  },
  animals: {
    name: '动物',
    icon: '🐱',
    emojis: [
      { char: '🐱', name: '猫脸' },
      { char: '🐶', name: '狗脸' },
      { char: '🐭', name: '老鼠' },
      { char: '🐹', name: '仓鼠' },
      { char: '🐰', name: '兔子' },
      { char: '🦊', name: '狐狸' },
      { char: '🐻', name: '熊' },
      { char: '🐼', name: '熊猫' },
      { char: '🐨', name: '考拉' },
      { char: '🐯', name: '老虎' },
      { char: '🦁', name: '狮子' },
      { char: '🐮', name: '牛' },
      { char: '🐷', name: '猪' },
      { char: '🐽', name: '猪鼻子' },
      { char: '🐸', name: '青蛙' },
      { char: '🐵', name: '猴子' },
      { char: '🙈', name: '不看' },
      { char: '🙉', name: '不听' },
      { char: '🙊', name: '不说' },
      { char: '🐒', name: '猴子' },
      { char: '🐔', name: '鸡' },
      { char: '🐧', name: '企鹅' },
      { char: '🐦', name: '鸟' },
      { char: '🐤', name: '小鸡' },
      { char: '🐣', name: '破壳小鸡' },
      { char: '🐥', name: '正面小鸡' },
      { char: '🦆', name: '鸭子' },
      { char: '🦅', name: '鹰' },
      { char: '🦉', name: '猫头鹰' },
      { char: '🦇', name: '蝙蝠' },
      { char: '🐺', name: '狼' },
      { char: '🐗', name: '野猪' },
      { char: '🐴', name: '马' },
      { char: '🦄', name: '独角兽' },
      { char: '🐝', name: '蜜蜂' },
      { char: '🪱', name: '虫子' },
      { char: '🐛', name: '毛毛虫' },
      { char: '🦋', name: '蝴蝶' },
      { char: '🐌', name: '蜗牛' },
      { char: '🐞', name: '瓢虫' },
      { char: '🐜', name: '蚂蚁' },
      { char: '🦟', name: '蚊子' },
      { char: '🦗', name: '蟋蟀' },
      { char: '🕷️', name: '蜘蛛' },
      { char: '🦂', name: '蝎子' },
      { char: '🐢', name: '乌龟' },
      { char: '🐍', name: '蛇' },
      { char: '🦎', name: '蜥蜴' },
      { char: '🐙', name: '章鱼' },
      { char: '🦑', name: '乌贼' },
      { char: '🦐', name: '虾' },
      { char: '🦞', name: '龙虾' },
      { char: '🦀', name: '螃蟹' },
      { char: '🐡', name: '河豚' },
      { char: '🐠', name: '热带鱼' },
      { char: '🐟', name: '鱼' },
      { char: '🐬', name: '海豚' },
      { char: '🐳', name: '鲸鱼' },
      { char: '🐋', name: '鲸' },
      { char: '🦈', name: '鲨鱼' },
      { char: '🐊', name: '鳄鱼' },
      { char: '🐆', name: '豹子' },
      { char: '🐅', name: '老虎' },
      { char: '🦓', name: '斑马' },
      { char: '🦍', name: '大猩猩' },
      { char: '🦧', name: '猩猩' },
      { char: '🐘', name: '大象' },
      { char: '🦛', name: '河马' },
      { char: '🦏', name: '犀牛' },
      { char: '🐪', name: '骆驼' },
      { char: '🐫', name: '双峰驼' },
      { char: '🦒', name: '长颈鹿' },
      { char: '🐃', name: '水牛' },
      { char: '🦬', name: '野牛' },
      { char: '🐂', name: '公牛' },
      { char: '🐄', name: '奶牛' },
      { char: '🐎', name: '马' },
      { char: '🐖', name: '猪' },
      { char: '🐏', name: '公羊' },
      { char: '🐑', name: '绵羊' },
      { char: '🦙', name: '羊驼' },
      { char: '🐐', name: '山羊' },
      { char: '🦌', name: '鹿' },
      { char: '🐕', name: '狗' },
      { char: '🐩', name: '贵宾犬' },
      { char: '🦮', name: '导盲犬' },
      { char: '🐕‍🦺', name: '服务犬' },
      { char: '🐈', name: '猫' },
      { char: '🐈‍⬛', name: '黑猫' },
      { char: '🪶', name: '羽毛' },
      { char: '🐓', name: '公鸡' },
      { char: '🦃', name: '火鸡' },
      { char: '🦤', name: '渡渡鸟' },
      { char: '🦚', name: '孔雀' },
      { char: '🦜', name: '鹦鹉' },
      { char: '🦢', name: '天鹅' },
      { char: '🦩', name: '火烈鸟' },
      { char: '🦔', name: '刺猬' },
      { char: '🐿️', name: '松鼠' }
    ]
  },
  food: {
    name: '食物',
    icon: '🍕',
    emojis: [
      { char: '🍕', name: '披萨' },
      { char: '🍔', name: '汉堡' },
      { char: '🍟', name: '薯条' },
      { char: '🌭', name: '热狗' },
      { char: '🍿', name: '爆米花' },
      { char: '🧂', name: '盐' },
      { char: '🥓', name: '培根' },
      { char: '🥚', name: '鸡蛋' },
      { char: '🍳', name: '煎蛋' },
      { char: '🧇', name: '华夫饼' },
      { char: '🥞', name: '煎饼' },
      { char: '🧈', name: '黄油' },
      { char: '🍞', name: '面包' },
      { char: '🥐', name: '牛角包' },
      { char: '🥖', name: '法棍' },
      { char: '🥨', name: '椒盐卷饼' },
      { char: '🧀', name: '奶酪' },
      { char: '🥗', name: '沙拉' },
      { char: '🥙', name: '皮塔饼' },
      { char: '🌮', name: '墨西哥卷' },
      { char: '🌯', name: '卷饼' },
      { char: '🫔', name: '玉米粽' },
      { char: '🥫', name: '罐头' },
      { char: '🍝', name: '意面' },
      { char: '🍜', name: '面条' },
      { char: '🍲', name: '火锅' },
      { char: '🍛', name: '咖喱' },
      { char: '🍣', name: '寿司' },
      { char: '🍱', name: '便当' },
      { char: '🥟', name: '饺子' },
      { char: '🦪', name: '牡蛎' },
      { char: '🍤', name: '炸虾' },
      { char: '🍙', name: '饭团' },
      { char: '🍚', name: '米饭' },
      { char: '🍘', name: '米饼' },
      { char: '🍥', name: '鱼板' },
      { char: '🥠', name: '幸运饼干' },
      { char: '🥡', name: '外卖盒' },
      { char: '🍦', name: '冰淇淋' },
      { char: '🍧', name: '刨冰' },
      { char: '🍨', name: '冰淇淋' },
      { char: '🍩', name: '甜甜圈' },
      { char: '🍪', name: '饼干' },
      { char: '🎂', name: '生日蛋糕' },
      { char: '🍰', name: '蛋糕' },
      { char: '🧁', name: '纸杯蛋糕' },
      { char: '🥧', name: '派' },
      { char: '🍫', name: '巧克力' },
      { char: '🍬', name: '糖果' },
      { char: '🍭', name: '棒棒糖' },
      { char: '🍮', name: '布丁' },
      { char: '🍯', name: '蜂蜜' },
      { char: '🍇', name: '葡萄' },
      { char: '🍈', name: '甜瓜' },
      { char: '🍉', name: '西瓜' },
      { char: '🍊', name: '橘子' },
      { char: '🍋', name: '柠檬' },
      { char: '🍌', name: '香蕉' },
      { char: '🍍', name: '菠萝' },
      { char: '🥭', name: '芒果' },
      { char: '🍎', name: '红苹果' },
      { char: '🍏', name: '青苹果' },
      { char: '🍐', name: '梨' },
      { char: '🍑', name: '桃子' },
      { char: '🍒', name: '樱桃' },
      { char: '🍓', name: '草莓' },
      { char: '🫐', name: '蓝莓' },
      { char: '🥝', name: '猕猴桃' },
      { char: '🍅', name: '番茄' },
      { char: '🫒', name: '橄榄' },
      { char: '🥥', name: '椰子' },
      { char: '🥑', name: '牛油果' },
      { char: '🍆', name: '茄子' },
      { char: '🥔', name: '土豆' },
      { char: '🥕', name: '胡萝卜' },
      { char: '🌽', name: '玉米' },
      { char: '🌶️', name: '辣椒' },
      { char: '🫑', name: '青椒' },
      { char: '🥒', name: '黄瓜' },
      { char: '🥬', name: '白菜' },
      { char: '🥦', name: '西兰花' },
      { char: '🧄', name: '大蒜' },
      { char: '🧅', name: '洋葱' },
      { char: '🍄', name: '蘑菇' },
      { char: '🥜', name: '花生' },
      { char: '🌰', name: '栗子' },
      { char: '☕', name: '咖啡' },
      { char: '🍵', name: '茶' },
      { char: '🧃', name: '果汁盒' },
      { char: '🥤', name: '饮料杯' },
      { char: '🧋', name: '奶茶' },
      { char: '🍺', name: '啤酒' },
      { char: '🍻', name: '干杯' },
      { char: '🥂', name: '香槟' },
      { char: '🍷', name: '红酒' },
      { char: '🥃', name: '威士忌' },
      { char: '🍸', name: '鸡尾酒' },
      { char: '🍹', name: '热带饮料' },
      { char: '🧉', name: '马黛茶' },
      { char: '🍾', name: '香槟瓶' }
    ]
  },
  objects: {
    name: '物品',
    icon: '💡',
    emojis: [
      { char: '💡', name: '灯泡' },
      { char: '🔦', name: '手电筒' },
      { char: '🏮', name: '灯笼' },
      { char: '📱', name: '手机' },
      { char: '💻', name: '电脑' },
      { char: '🖥️', name: '台式机' },
      { char: '🖨️', name: '打印机' },
      { char: '⌨️', name: '键盘' },
      { char: '🖱️', name: '鼠标' },
      { char: '💾', name: '软盘' },
      { char: '💿', name: '光盘' },
      { char: '📀', name: 'DVD' },
      { char: '📷', name: '相机' },
      { char: '📸', name: '闪光相机' },
      { char: '📹', name: '摄像机' },
      { char: '🎥', name: '电影摄像机' },
      { char: '📽️', name: '放映机' },
      { char: '📞', name: '电话' },
      { char: '☎️', name: '老式电话' },
      { char: '📟', name: '寻呼机' },
      { char: '📺', name: '电视' },
      { char: '📻', name: '收音机' },
      { char: '🎙️', name: '麦克风' },
      { char: '🎚️', name: '调音台' },
      { char: '🎛️', name: '控制旋钮' },
      { char: '⏱️', name: '秒表' },
      { char: '⏲️', name: '计时器' },
      { char: '⏰', name: '闹钟' },
      { char: '🕰️', name: '座钟' },
      { char: '⌚', name: '手表' },
      { char: '📡', name: '卫星天线' },
      { char: '🔋', name: '电池' },
      { char: '🔌', name: '插头' },
      { char: '💰', name: '钱袋' },
      { char: '💴', name: '日元' },
      { char: '💵', name: '美元' },
      { char: '💶', name: '欧元' },
      { char: '💷', name: '英镑' },
      { char: '💳', name: '信用卡' },
      { char: '💎', name: '钻石' },
      { char: '⚖️', name: '天平' },
      { char: '🔧', name: '扳手' },
      { char: '🔨', name: '锤子' },
      { char: '⚒️', name: '锤子和镐' },
      { char: '🛠️', name: '工具' },
      { char: '⛏️', name: '镐' },
      { char: '🔩', name: '螺丝' },
      { char: '⚙️', name: '齿轮' },
      { char: '🔗', name: '链接' },
      { char: '📎', name: '曲别针' },
      { char: '🖇️', name: '连接曲别针' },
      { char: '📐', name: '三角尺' },
      { char: '📏', name: '直尺' },
      { char: '✂️', name: '剪刀' },
      { char: '📌', name: '图钉' },
      { char: '📍', name: '大头钉' },
      { char: '🔒', name: '锁' },
      { char: '🔓', name: '开锁' },
      { char: '🔑', name: '钥匙' },
      { char: '🗝️', name: '老式钥匙' },
      { char: '🔐', name: '密码锁' },
      { char: '📝', name: '备忘录' },
      { char: '📚', name: '书' },
      { char: '📖', name: '打开的书' },
      { char: '📰', name: '报纸' },
      { char: '📃', name: '带卷的纸' },
      { char: '📄', name: '纸' },
      { char: '📑', name: '书签标签' },
      { char: '🔖', name: '书签' },
      { char: '📓', name: '笔记本' },
      { char: '📔', name: '精装笔记本' },
      { char: '📕', name: '红皮书' },
      { char: '📗', name: '绿皮书' },
      { char: '📘', name: '蓝皮书' },
      { char: '📙', name: '橙皮书' },
      { char: '✏️', name: '铅笔' },
      { char: '✒️', name: '钢笔' },
      { char: '🖊️', name: '圆珠笔' },
      { char: '🖋️', name: '钢笔' },
      { char: '🖌️', name: '画笔' },
      { char: '🖍️', name: '蜡笔' },
      { char: '🎁', name: '礼物' },
      { char: '🎀', name: '蝴蝶结' },
      { char: '🎈', name: '气球' },
      { char: '🎉', name: '派对' },
      { char: '🎊', name: '彩纸' },
      { char: '🎄', name: '圣诞树' },
      { char: '🎃', name: '南瓜' },
      { char: '🔮', name: '水晶球' },
      { char: '🧿', name: '护身符' },
      { char: '🎲', name: '骰子' },
      { char: '♟️', name: '棋子' },
      { char: '🎯', name: '靶心' },
      { char: '🎱', name: '台球' },
      { char: '🎮', name: '游戏手柄' },
      { char: '🕹️', name: '操纵杆' },
      { char: '🎰', name: '老虎机' },
      { char: '🎵', name: '音符' },
      { char: '🎶', name: '音乐' },
      { char: '🎷', name: '萨克斯' },
      { char: '🎸', name: '吉他' },
      { char: '🎹', name: '钢琴' },
      { char: '🎺', name: '小号' },
      { char: '🎻', name: '小提琴' },
      { char: '🪕', name: '班卓琴' },
      { char: '🥁', name: '鼓' }
    ]
  },
  symbols: {
    name: '符号',
    icon: '⭐',
    emojis: [
      { char: '⭐', name: '星星' },
      { char: '🌟', name: '闪亮星' },
      { char: '✨', name: '闪光' },
      { char: '💫', name: '头晕' },
      { char: '⚡', name: '闪电' },
      { char: '🔥', name: '火焰' },
      { char: '💥', name: '爆炸' },
      { char: '☀️', name: '太阳' },
      { char: '🌈', name: '彩虹' },
      { char: '☁️', name: '云' },
      { char: '❄️', name: '雪花' },
      { char: '💧', name: '水滴' },
      { char: '🌊', name: '海浪' },
      { char: '✅', name: '勾选' },
      { char: '❌', name: '叉' },
      { char: '❓', name: '问号' },
      { char: '❗', name: '感叹号' },
      { char: '⭕', name: '圆圈' },
      { char: '🔴', name: '红色圆圈' },
      { char: '🟠', name: '橙色圆圈' },
      { char: '🟡', name: '黄色圆圈' },
      { char: '🟢', name: '绿色圆圈' },
      { char: '🔵', name: '蓝色圆圈' },
      { char: '🟣', name: '紫色圆圈' },
      { char: '⚫', name: '黑色圆圈' },
      { char: '⚪', name: '白色圆圈' },
      { char: '🟤', name: '棕色圆圈' },
      { char: '🔺', name: '红色三角形' },
      { char: '🔻', name: '倒三角形' },
      { char: '🔷', name: '蓝色菱形' },
      { char: '🔶', name: '橙色菱形' },
      { char: '💠', name: '钻石形' },
      { char: '🔘', name: '单选按钮' },
      { char: '🔳', name: '白色方框' },
      { char: '🔲', name: '黑色方框' },
      { char: '▶️', name: '播放' },
      { char: '⏸️', name: '暂停' },
      { char: '⏹️', name: '停止' },
      { char: '⏯️', name: '播放暂停' },
      { char: '⏭️', name: '下一曲' },
      { char: '⏮️', name: '上一曲' },
      { char: '🔀', name: '随机播放' },
      { char: '🔁', name: '循环' },
      { char: '🔂', name: '单曲循环' },
      { char: '🔊', name: '音量高' },
      { char: '🔉', name: '音量中' },
      { char: '🔈', name: '音量低' },
      { char: '🔇', name: '静音' },
      { char: '📢', name: '喇叭' },
      { char: '📣', name: '扩音器' },
      { char: '🔔', name: '铃铛' },
      { char: '🔕', name: '静音铃铛' },
      { char: '💬', name: '对话' },
      { char: '💭', name: '思考' },
      { char: '🗨️', name: '对话框' },
      { char: '🗯️', name: '愤怒对话' },
      { char: '♠️', name: '黑桃' },
      { char: '♣️', name: '梅花' },
      { char: '♥️', name: '红心' },
      { char: '♦️', name: '方块' },
      { char: '🃏', name: '王牌' },
      { char: '🀄', name: '麻将' },
      { char: '🎴', name: '花札' },
      { char: '➕', name: '加号' },
      { char: '➖', name: '减号' },
      { char: '✖️', name: '乘号' },
      { char: '➗', name: '除号' },
      { char: '💯', name: '满分' },
      { char: '🔢', name: '数字' },
      { char: '🔣', name: '符号' },
      { char: '🔤', name: '字母' },
      { char: '🅰️', name: 'A' },
      { char: '🆎', name: 'AB' },
      { char: '🅱️', name: 'B' },
      { char: '🆑', name: 'CL' },
      { char: '🆒', name: 'COOL' },
      { char: '🆓', name: 'FREE' },
      { char: 'ℹ️', name: '信息' },
      { char: '🆔', name: 'ID' },
      { char: 'Ⓜ️', name: 'M圈' },
      { char: '🆕', name: 'NEW' },
      { char: '🆖', name: 'NG' },
      { char: '🅾️', name: 'O' },
      { char: '🆗', name: 'OK' },
      { char: '🅿️', name: 'P' },
      { char: '🆘', name: 'SOS' },
      { char: '🆙', name: 'UP!' },
      { char: '🆚', name: 'VS' },
      { char: '🈁', name: '日文按钮' },
      { char: '🈂️', name: '服务标志' },
      { char: '🈷️', name: '月' },
      { char: '🈶', name: '有' },
      { char: '🈯', name: '指' },
      { char: '🉐', name: '得' },
      { char: '🈹', name: '割' },
      { char: '🈚', name: '无' },
      { char: '🈲', name: '禁' },
      { char: '🉑', name: '可' },
      { char: '🈸', name: '申' },
      { char: '🈴', name: '合' },
      { char: '🈳', name: '空' },
      { char: '㊗️', name: '祝' },
      { char: '㊙️', name: '秘' },
      { char: '🈺', name: '营' },
      { char: '🈵', name: '满' }
    ]
  }
}

// LocalStorage key for recent emojis
const RECENT_EMOJIS_KEY = 'blog_recent_emojis'
// Expiration time: 3 days in milliseconds
const EXPIRATION_MS = 3 * 24 * 60 * 60 * 1000
// Maximum number of recent emojis
const MAX_RECENT = 12

export default {
  name: 'EmojiPicker',
  props: {
    title: {
      type: String,
      default: '表情'
    }
  },
  emits: ['select'],
  setup(props, { emit }) {
    const isOpen = ref(false)
    const activeCategory = ref('smileys')
    const recentEmojis = ref([])
    const triggerButton = ref(null)
    const pickerDropdown = ref(null)
    const dropdownStyle = ref({})

    // Categories for tabs
    const categories = computed(() => {
      return Object.entries(EMOJI_DATA).map(([id, data]) => ({
        id,
        name: data.name,
        icon: data.icon
      }))
    })

    // Current emojis based on active category
    const currentEmojis = computed(() => {
      return EMOJI_DATA[activeCategory.value]?.emojis || []
    })

    // Load recent emojis from localStorage
    const loadRecentEmojis = () => {
      try {
        const stored = localStorage.getItem(RECENT_EMOJIS_KEY)
        if (!stored) {
          recentEmojis.value = []
          return
        }

        const data = JSON.parse(stored)
        const now = Date.now()

        // Filter out expired emojis
        const validEmojis = data.filter(item => {
          return now - item.timestamp < EXPIRATION_MS
        })

        // Update localStorage with valid emojis only
        if (validEmojis.length !== data.length) {
          saveRecentEmojis(validEmojis)
        }

        recentEmojis.value = validEmojis.map(item => ({
          char: item.char,
          name: item.name
        }))
      } catch (e) {
        console.error('Failed to load recent emojis:', e)
        recentEmojis.value = []
      }
    }

    // Save recent emojis to localStorage
    const saveRecentEmojis = (emojis) => {
      try {
        localStorage.setItem(RECENT_EMOJIS_KEY, JSON.stringify(emojis))
      } catch (e) {
        console.error('Failed to save recent emojis:', e)
      }
    }

    // Add emoji to recent list
    const addToRecent = (emoji) => {
      try {
        const stored = localStorage.getItem(RECENT_EMOJIS_KEY)
        let data = stored ? JSON.parse(stored) : []

        // Remove existing entry if present (to update timestamp)
        data = data.filter(item => item.char !== emoji.char)

        // Add new entry at the beginning
        data.unshift({
          char: emoji.char,
          name: emoji.name,
          timestamp: Date.now()
        })

        // Limit to MAX_RECENT items
        if (data.length > MAX_RECENT) {
          data = data.slice(0, MAX_RECENT)
        }

        saveRecentEmojis(data)
        loadRecentEmojis()
      } catch (e) {
        console.error('Failed to add to recent emojis:', e)
      }
    }

    // Toggle picker visibility
    const togglePicker = () => {
      if (isOpen.value) {
        closePicker()
      } else {
        openPicker()
      }
    }

    // Open picker
    const openPicker = async () => {
      loadRecentEmojis()
      isOpen.value = true
      await nextTick()
      calculateDropdownPosition()
    }

    // Close picker
    const closePicker = () => {
      isOpen.value = false
    }

    // Calculate dropdown position
    const calculateDropdownPosition = () => {
      if (!triggerButton.value) return

      const rect = triggerButton.value.getBoundingClientRect()
      const viewportWidth = window.innerWidth
      const viewportHeight = window.innerHeight
      const dropdownWidth = 320
      const dropdownHeight = 350

      let left = 0
      let top = '100%'

      // Check if dropdown would go off the right edge
      if (rect.left + dropdownWidth > viewportWidth) {
        left = viewportWidth - rect.left - dropdownWidth - 10
      }

      // Check if dropdown would go off the bottom edge
      if (rect.bottom + dropdownHeight > viewportHeight) {
        top = 'auto'
        dropdownStyle.value = {
          left: `${left}px`,
          bottom: '100%',
          marginBottom: '4px'
        }
      } else {
        dropdownStyle.value = {
          left: `${left}px`,
          top: top,
          marginTop: '4px'
        }
      }
    }

    // Select emoji
    const selectEmoji = (emoji) => {
      addToRecent(emoji)
      emit('select', emoji.char)
      closePicker()
    }

    // Handle click outside
    const handleClickOutside = (event) => {
      if (
        isOpen.value &&
        triggerButton.value &&
        pickerDropdown.value &&
        !triggerButton.value.contains(event.target) &&
        !pickerDropdown.value.contains(event.target)
      ) {
        closePicker()
      }
    }

    // Handle escape key
    const handleEscapeKey = (event) => {
      if (event.key === 'Escape' && isOpen.value) {
        closePicker()
      }
    }

    onMounted(() => {
      loadRecentEmojis()
      document.addEventListener('click', handleClickOutside)
      document.addEventListener('keydown', handleEscapeKey)
    })

    onBeforeUnmount(() => {
      document.removeEventListener('click', handleClickOutside)
      document.removeEventListener('keydown', handleEscapeKey)
    })

    return {
      isOpen,
      activeCategory,
      recentEmojis,
      categories,
      currentEmojis,
      triggerButton,
      pickerDropdown,
      dropdownStyle,
      togglePicker,
      closePicker,
      selectEmoji
    }
  }
}
</script>

<style scoped>
.emoji-btn {
  @apply w-8 h-8 flex items-center justify-center text-xl rounded hover:bg-gray-100 transition-colors cursor-pointer;
}

.emoji-btn:hover {
  transform: scale(1.2);
}

.toolbar-btn {
  @apply px-2.5 py-1.5 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100
  transition-colors duration-150 flex items-center justify-center min-w-[32px];
}

.toolbar-btn:active {
  @apply bg-gray-200;
}

.toolbar-btn:focus {
  @apply outline-none ring-2 ring-primary-500 ring-opacity-50;
}
</style>
