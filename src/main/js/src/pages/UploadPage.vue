<template>
  <q-page>

    <div class="row flex flex-center">
      <h3>Upload Page</h3>
    </div>
    <div class="row flex flex-center">
      <q-file v-model="fileToUpload" dense outlined accept=".tf" />
      <q-btn push color="primary" round icon="file_upload" @click="handleFileUpload" />
    </div>

    <div class="row flex flex-center">
      <q-table
        :rows="data"
        :columns="columns"
        style="width: 70%"
      >
        <template v-slot:body-cell-uploadedAt="props">
          <q-td :props="props">
            <span>
              {{ this.formatDateRelative(props.row.uploadedAt) }}
              <q-tooltip>
                {{ this.formatDateISO(props.row.uploadedAt) }}
              </q-tooltip>
            </span>
          </q-td>
        </template>

        <template v-slot:body-cell-action="props">
          <q-td :props="props">
            <div>
              <q-btn icon="delete" flat dense @click="deleteState(props.row)"></q-btn>
            </div>
          </q-td>
        </template>

      </q-table>
    </div>

  </q-page>
</template>

<script>
import { useQuasar } from 'quasar';
import { ref } from 'vue';
import { DateTime } from 'luxon';

export default {
  setup() {
    const q = useQuasar();
    return { q };
  },
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: 'Name',
          align: 'left',
          field: 'name',
          sortable: true,
        },
        {
          name: 'uploadedAt',
          label: 'Uploaded At',
          align: 'left',
          field: 'uploadedAt',
          sortable: true,
        },
        {
          name: 'action',
          label: '',
          align: 'right',
          field: 'action',
        },
      ],
      data: [],
      selectedFile: false,
      uploaderLabel: 'Select a File',
      showUploadButton: false,
      fileToUpload: ref(null),
    };
  },
  mounted() {
    this.getStates();
  },
  methods: {
    formatDateRelative(val) {
      return DateTime.fromMillis(val * 1000).toRelative();
    },
    formatDateISO(val) {
      return DateTime.fromMillis(val * 1000).toISO();
    },
    getStates() {
      this.$api.get('/states')
        .then((response) => {
          this.data = response.data;
        });
    },
    deleteState(row) {
      this.q.dialog({
        title: 'Confirm',
        message: `Delete '${row.name}' uploaded ${this.formatDateRelative(row.uploadedAt)}?`,
        ok: {
          push: true,
          label: 'Delete State',
          tabindex: 1,
        },
        cancel: {
          push: true,
          color: 'negative',
          label: 'Cancel',
          tabindex: 0,
        },
        persistent: true,
      }).onOk(() => {
        this.$api.delete(`/states/${row.id}`)
          .then(() => this.getStates());
      });
    },
    handleFileUpload() {
      const formData = new FormData();
      formData.append('name', this.fileToUpload.name);
      formData.append('file', this.fileToUpload);

      const config = {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      };

      this.$api.post(
        '/states',
        formData,
        config,
      ).then(() => this.resetFileInput());
    },
    resetFileInput() {
      this.fileToUpload = null;
      this.getStates();
    },
  },
};
</script>
