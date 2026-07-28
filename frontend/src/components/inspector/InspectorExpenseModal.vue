<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div class="bg-white rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden flex flex-col">
        <div class="p-5 border-b border-slate-100 flex items-center justify-between">
          <h3 class="text-lg font-black text-slate-900">Khai báo chi phí</h3>
          <button @click="$emit('close')" class="w-8 h-8 flex items-center justify-center rounded-full bg-slate-100 text-slate-500">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>
        
        <div class="max-h-[70vh] overflow-y-auto p-5 space-y-6 bg-white">
          <div v-for="(exp, index) in expensesList" :key="exp.id" class="p-5 bg-slate-50 border border-slate-200 rounded-2xl relative">
            
            <button v-if="expensesList.length > 1" @click="removeExpense(index)" class="absolute -top-3 -right-3 w-8 h-8 rounded-full bg-red-100 text-red-600 flex items-center justify-center shadow-sm hover:bg-red-200 transition-colors">
              <span class="material-symbols-outlined text-[18px]">close</span>
            </button>
            
            <h4 class="text-sm font-black text-slate-700 mb-4 pb-2 border-b border-slate-200">Khoản chi #{{ index + 1 }}</h4>

            <div class="space-y-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-500 uppercase tracking-widest">Loại chi phí</label>
                <select v-model="exp.expenseType" class="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-bold focus:outline-none focus:border-emerald-500">
                  <option value="FUEL">Tiền nhiên liệu</option>
                  <option value="TOLL">Phí cầu đường/BOT</option>
                  <option value="FOOD">Ăn ca</option>
                  <option value="REPAIR">Sửa chữa</option>
                  <option value="OTHER">Khác</option>
                </select>
              </div>
              
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-500 uppercase tracking-widest">Số tiền (VNĐ)</label>
                <input type="number" v-model="exp.amount" placeholder="VD: 500000" class="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-black focus:outline-none focus:border-emerald-500" />
              </div>
              
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-500 uppercase tracking-widest">Ghi chú (Tùy chọn)</label>
                <textarea v-model="exp.description" rows="2" placeholder="Ghi chú thêm..." class="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-semibold focus:outline-none focus:border-emerald-500"></textarea>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-500 uppercase tracking-widest">Ảnh hóa đơn</label>
                <input type="file" @change="(e) => onFileChange(e, index)" accept="image/*" class="w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-bold file:bg-emerald-50 file:text-emerald-700 hover:file:bg-emerald-100" />
                <p class="text-[10px] text-slate-400 mt-1">Nên chụp ảnh bill để Kế toán duyệt nhanh hơn.</p>
              </div>
            </div>
          </div>

          <button @click="addExpense" class="w-full py-3 bg-slate-50 border-2 border-dashed border-slate-300 rounded-xl text-slate-500 font-bold hover:bg-slate-100 hover:text-slate-700 transition-colors flex items-center justify-center gap-2">
            <span class="material-symbols-outlined">add</span> Thêm khoản chi phí khác
          </button>
        </div>
        
        <div class="p-4 border-t border-slate-100 bg-slate-50">
          <button @click="submit" :disabled="submitting || !hasValidExpense" class="w-full py-3.5 bg-emerald-600 text-white rounded-xl font-black shadow-lg shadow-emerald-600/30 disabled:opacity-50 flex items-center justify-center gap-2">
            <span v-if="submitting" class="material-symbols-outlined animate-spin text-[20px]">progress_activity</span>
            Gửi {{ validExpensesCount }} Yêu Cầu
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  show: Boolean,
  tripId: Number
})

const emit = defineEmits(['close', 'refresh'])

const submitting = ref(false)

const getEmptyExpense = () => ({
  id: Date.now() + Math.random(),
  expenseType: 'FUEL',
  amount: null,
  description: '',
  selectedFile: null
})

const expensesList = ref([getEmptyExpense()])

const validExpensesCount = computed(() => expensesList.value.filter(e => e.amount > 0).length)
const hasValidExpense = computed(() => validExpensesCount.value > 0)

const addExpense = () => {
  expensesList.value.push(getEmptyExpense())
}

const removeExpense = (index) => {
  if (expensesList.value.length > 1) {
    expensesList.value.splice(index, 1)
  }
}

const onFileChange = (e, index) => {
  if (e.target.files && e.target.files[0]) {
    expensesList.value[index].selectedFile = e.target.files[0]
  }
}

const submit = async () => {
  const validExpenses = expensesList.value.filter(e => e.amount > 0)
  if (validExpenses.length === 0) return
  
  submitting.value = true
  
  try {
    const token = localStorage.getItem('jwt_token')
    const userPhone = localStorage.getItem('user_phone')
    let hasError = false;

    // Post each expense sequentially
    for (const exp of validExpenses) {
      let receiptImageUrl = null;
      
      // Upload image to Cloudinary if selected
      if (exp.selectedFile) {
        const uploadData = new FormData();
        uploadData.append('file', exp.selectedFile);
        uploadData.append('upload_preset', 'skybus_preset');
        
        try {
          const uploadRes = await fetch('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', {
            method: 'POST',
            body: uploadData
          });
          if (uploadRes.ok) {
            const uploadJson = await uploadRes.json();
            receiptImageUrl = uploadJson.secure_url;
          }
        } catch (uploadErr) {
          console.error("Cloudinary error:", uploadErr);
        }
      }

      const payload = {
        amount: exp.amount,
        expenseType: exp.expenseType,
        description: exp.description,
        reportedBy: userPhone,
        receiptImageUrl: receiptImageUrl
      };

      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${props.tripId}/expenses`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })
      
      if (!res.ok) {
        hasError = true;
      }
    }
    
    if (hasError) {
      alert('Một vài khoản chi phí có thể không được gửi thành công. Vui lòng kiểm tra lại lịch sử.')
    }

    expensesList.value = [getEmptyExpense()]
    emit('refresh')
    emit('close')
    
  } catch (error) {
    console.error(error)
    alert('Đã xảy ra lỗi: ' + error.message)
  } finally {
    submitting.value = false
  }
}
</script>
