<template>
  <section class="route-config-card space-y-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-[10px] font-black uppercase tracking-wider text-slate-600">
          Nhóm biển số xe của tuyến
        </p>
        <p class="mt-1 text-[10px] font-semibold text-slate-400">
          Xe chính được ưu tiên, xe dự phòng dùng khi thiếu phương tiện.
        </p>
      </div>
      <label class="route-config-toggle flex shrink-0 cursor-pointer items-center gap-2 text-[10px] font-black text-[#075955]">
        <input
          :checked="unrestricted"
          type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-[#075955] focus:ring-[#075955]"
          @change="$emit('update:unrestricted', $event.target.checked)"
        />
        Tất cả xe đúng dòng
      </label>
    </div>

    <div v-if="loading" class="py-3 text-center text-xs font-bold text-slate-400">
      Đang tải nhóm phương tiện...
    </div>

    <div v-else-if="!eligibleBuses.length" class="rounded-xl bg-amber-50 px-3 py-2 text-xs font-bold text-amber-700">
      Không có xe nào thuộc các dòng xe đã chọn.
    </div>

    <div v-else class="max-h-56 space-y-2 overflow-y-auto pr-1" :class="{ 'opacity-50': unrestricted }">
      <div
        v-for="bus in eligibleBuses"
        :key="bus.id"
        class="route-vehicle-row flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-3 py-3"
      >
        <span class="material-symbols-outlined text-lg text-slate-400">directions_bus</span>
        <div class="min-w-0 flex-1">
          <p class="truncate text-xs font-black text-slate-700">{{ bus.licensePlate }}</p>
          <p class="truncate text-[9px] font-semibold text-slate-400">
            {{ bus.busType }}
          </p>
        </div>
        <select
          :value="roleOf(bus.id)"
          :disabled="unrestricted"
          class="w-28 rounded-lg border border-slate-200 bg-white px-2 py-1.5 text-[10px] font-black outline-none focus:border-[#075955]"
          @change="setRole(bus.id, $event.target.value)"
        >
          <option value="">Không gán</option>
          <option value="PRIMARY">Xe chính</option>
          <option value="BACKUP">Dự phòng</option>
        </select>
      </div>
    </div>

    <div v-if="!unrestricted" class="flex gap-4 border-t border-slate-200 pt-3 text-[10px] font-black">
      <span class="text-emerald-700">{{ primaryBusIds.length }} xe chính</span>
      <span class="text-amber-600">{{ backupBusIds.length }} xe dự phòng</span>
    </div>
    <p v-if="!unrestricted && !primaryBusIds.length && !backupBusIds.length" class="text-[10px] font-bold text-rose-500">
      Vui lòng gán ít nhất một phương tiện cho tuyến.
    </p>
  </section>
</template>

<script setup>
import { computed, watch } from 'vue';

const props = defineProps({
  buses: { type: Array, default: () => [] },
  busTypes: { type: Array, default: () => [] },
  allowedBusTypeIds: { type: Array, default: () => [] },
  unrestrictedBusTypes: { type: Boolean, default: true },
  primaryBusIds: { type: Array, default: () => [] },
  backupBusIds: { type: Array, default: () => [] },
  unrestricted: { type: Boolean, default: true },
  loading: { type: Boolean, default: false }
});

const emit = defineEmits([
  'update:primaryBusIds',
  'update:backupBusIds',
  'update:unrestricted'
]);

const allowedTypeNames = computed(() => {
  if (props.unrestrictedBusTypes) return null;
  return new Set(
    props.busTypes
      .filter(type => props.allowedBusTypeIds.includes(type.id))
      .map(type => type.name.toLocaleLowerCase('vi-VN'))
  );
});

const eligibleBuses = computed(() => {
  const buses = allowedTypeNames.value
    ? props.buses.filter(bus => allowedTypeNames.value.has(String(bus.busType).toLocaleLowerCase('vi-VN')))
    : props.buses;
  return [...buses].sort((a, b) => String(a.licensePlate).localeCompare(String(b.licensePlate), 'vi'));
});

const roleOf = busId => {
  if (props.primaryBusIds.includes(busId)) return 'PRIMARY';
  if (props.backupBusIds.includes(busId)) return 'BACKUP';
  return '';
};

const setRole = (busId, role) => {
  const primary = props.primaryBusIds.filter(id => id !== busId);
  const backup = props.backupBusIds.filter(id => id !== busId);
  if (role === 'PRIMARY') primary.push(busId);
  if (role === 'BACKUP') backup.push(busId);
  emit('update:primaryBusIds', primary);
  emit('update:backupBusIds', backup);
};

watch(eligibleBuses, buses => {
  if (props.loading || !props.buses.length) return;
  const eligibleIds = new Set(buses.map(bus => bus.id));
  const primary = props.primaryBusIds.filter(id => eligibleIds.has(id));
  const backup = props.backupBusIds.filter(id => eligibleIds.has(id));
  if (primary.length !== props.primaryBusIds.length) emit('update:primaryBusIds', primary);
  if (backup.length !== props.backupBusIds.length) emit('update:backupBusIds', backup);
});
</script>

<style scoped>
.route-config-card {
  border: 1px solid #dce7e5;
  border-radius: 18px;
  background: #fff;
  padding: 1.25rem;
  box-shadow: 0 8px 24px rgba(18, 78, 73, 0.055);
}

.route-config-toggle {
  border-radius: 10px;
  background: #eef7f5;
  padding: 0.55rem 0.7rem;
}

.route-vehicle-row {
  transition: border-color 180ms ease, background-color 180ms ease, transform 180ms ease;
}

.route-vehicle-row:hover {
  border-color: #aac8c4;
  background: #fbfdfd;
  transform: translateY(-1px);
}
</style>
