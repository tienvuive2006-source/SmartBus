<template>
  <aside class="hidden lg:block space-y-6 sticky top-28 self-start">
    <div class="bg-white rounded-[2rem] border border-zinc-100/80 overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
      <div class="p-6 pb-4 flex justify-between items-center">
         <h3 class="text-[11px] font-black uppercase tracking-widest text-zinc-900">Sắp xếp theo</h3>
         <button @click="$emit('clear-filters')" class="text-[10px] font-bold text-zinc-400 hover:text-red-500 active:scale-95 transition-colors uppercase tracking-widest cursor-pointer">Xóa lọc</button>
      </div>
      <div class="px-3 pb-6 flex flex-col gap-1">
         <label v-for="sort in sortOptions" :key="sort.id" class="relative flex items-center px-4 py-3 cursor-pointer group rounded-xl transition-all" :class="modelValueSort === sort.id ? 'bg-emerald-50/50' : 'hover:bg-zinc-50'">
            <input type="radio" name="sort" :value="sort.id" :checked="modelValueSort === sort.id" @change="$emit('update:modelValueSort', sort.id)" class="absolute opacity-0 w-0 h-0" />
            <div class="w-4 h-4 rounded-full border-2 mr-3 flex items-center justify-center transition-colors shrink-0" :class="modelValueSort === sort.id ? 'border-[#075955]' : 'border-zinc-200 group-hover:border-zinc-300'">
               <div class="w-1.5 h-1.5 rounded-full bg-[#075955] transition-transform duration-300" :class="modelValueSort === sort.id ? 'scale-100' : 'scale-0'"></div>
            </div>
            <span class="text-[13px] font-bold transition-colors" :class="modelValueSort === sort.id ? 'text-[#075955]' : 'text-zinc-500 group-hover:text-zinc-900'">{{ sort.name }}</span>
         </label>
      </div>
    </div>

    <div class="bg-white rounded-[2rem] border border-zinc-100/80 overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
      <div class="p-6 pb-4">
         <h3 class="text-[11px] font-black uppercase tracking-widest text-zinc-900">Giờ khởi hành</h3>
      </div>
      <div class="px-3 pb-6 flex flex-col gap-1">
         <label class="relative flex items-center px-4 py-3 cursor-pointer group rounded-xl transition-all hover:bg-zinc-50">
           <input type="checkbox" :value="'early'" :checked="modelValueTime.includes('early')" @change="updateTimeSlots('early', $event.target.checked)" class="absolute opacity-0 w-0 h-0"/>
           <div class="w-5 h-5 rounded-md border-2 mr-3 flex items-center justify-center transition-all shrink-0" :class="modelValueTime.includes('early') ? 'bg-[#075955] border-[#075955]' : 'bg-white border-zinc-200 group-hover:border-zinc-300'">
             <span class="material-symbols-outlined text-[14px] text-white transition-opacity duration-300" :class="modelValueTime.includes('early') ? 'opacity-100' : 'opacity-0'">check</span>
           </div>
           <div class="flex flex-col">
             <span class="text-[13px] font-bold text-zinc-800">Sáng sớm</span>
             <span class="text-[10px] font-semibold text-zinc-400">00:00 - 06:00</span>
           </div>
         </label>

         <label class="relative flex items-center px-4 py-3 cursor-pointer group rounded-xl transition-all hover:bg-zinc-50">
           <input type="checkbox" :value="'morning'" :checked="modelValueTime.includes('morning')" @change="updateTimeSlots('morning', $event.target.checked)" class="absolute opacity-0 w-0 h-0"/>
           <div class="w-5 h-5 rounded-md border-2 mr-3 flex items-center justify-center transition-all shrink-0" :class="modelValueTime.includes('morning') ? 'bg-[#075955] border-[#075955]' : 'bg-white border-zinc-200 group-hover:border-zinc-300'">
             <span class="material-symbols-outlined text-[14px] text-white transition-opacity duration-300" :class="modelValueTime.includes('morning') ? 'opacity-100' : 'opacity-0'">check</span>
           </div>
           <div class="flex flex-col">
             <span class="text-[13px] font-bold text-zinc-800">Buổi sáng</span>
             <span class="text-[10px] font-semibold text-zinc-400">06:00 - 12:00</span>
           </div>
         </label>

         <label class="relative flex items-center px-4 py-3 cursor-pointer group rounded-xl transition-all hover:bg-zinc-50">
           <input type="checkbox" :value="'afternoon'" :checked="modelValueTime.includes('afternoon')" @change="updateTimeSlots('afternoon', $event.target.checked)" class="absolute opacity-0 w-0 h-0"/>
           <div class="w-5 h-5 rounded-md border-2 mr-3 flex items-center justify-center transition-all shrink-0" :class="modelValueTime.includes('afternoon') ? 'bg-[#075955] border-[#075955]' : 'bg-white border-zinc-200 group-hover:border-zinc-300'">
             <span class="material-symbols-outlined text-[14px] text-white transition-opacity duration-300" :class="modelValueTime.includes('afternoon') ? 'opacity-100' : 'opacity-0'">check</span>
           </div>
           <div class="flex flex-col">
             <span class="text-[13px] font-bold text-zinc-800">Buổi chiều</span>
             <span class="text-[10px] font-semibold text-zinc-400">12:00 - 18:00</span>
           </div>
         </label>

         <label class="relative flex items-center px-4 py-3 cursor-pointer group rounded-xl transition-all hover:bg-zinc-50">
           <input type="checkbox" :value="'evening'" :checked="modelValueTime.includes('evening')" @change="updateTimeSlots('evening', $event.target.checked)" class="absolute opacity-0 w-0 h-0"/>
           <div class="w-5 h-5 rounded-md border-2 mr-3 flex items-center justify-center transition-all shrink-0" :class="modelValueTime.includes('evening') ? 'bg-[#075955] border-[#075955]' : 'bg-white border-zinc-200 group-hover:border-zinc-300'">
             <span class="material-symbols-outlined text-[14px] text-white transition-opacity duration-300" :class="modelValueTime.includes('evening') ? 'opacity-100' : 'opacity-0'">check</span>
           </div>
           <div class="flex flex-col">
             <span class="text-[13px] font-bold text-zinc-800">Buổi tối</span>
             <span class="text-[10px] font-semibold text-zinc-400">18:00 - 24:00</span>
           </div>
         </label>
      </div>
    </div>
  </aside>
</template>

<script setup>
const props = defineProps({
  modelValueSort: { type: String, required: true },
  modelValueTime: { type: Array, required: true },
  sortOptions: { type: Array, required: true }
});

const emit = defineEmits(['update:modelValueSort', 'update:modelValueTime', 'clear-filters']);

const updateTimeSlots = (val, checked) => {
  let newArr = [...props.modelValueTime];
  if (checked) {
    newArr.push(val);
  } else {
    newArr = newArr.filter(i => i !== val);
  }
  emit('update:modelValueTime', newArr);
};
</script>
