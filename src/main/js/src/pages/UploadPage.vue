<template>
  <q-page>

    <div class="row flex flex-center">
      <h3>Upload Page</h3>
    </div>
    <div class="row flex flex-center">
      <input type="file" accept=".tf" ref="fileupload" @change="handleFileUpload( $event )"/>
    </div>

    <div class="row flex flex-center">
      <q-table
        :rows="data"
        :columns="columns"
        style="width: 70%"
      >

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
import { api } from 'boot/axios';

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
    };
  },
  mounted() {
    this.getStates();
  },
  methods: {
    getStates() {
      api.get('/states')
        .then((response) => {
          this.data = response.data;
        });
    },
    deleteState(row) {
      this.q.dialog({
        title: 'Confirm',
        message: `Delete '${row.name}' uploaded at ${row.uploadedAt}?`,
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
        api.delete(`/states/${row.id}`)
          .then(() => this.getStates());
      });
    },
    handleFileUpload(event) {
      const formData = new FormData();
      formData.append('name', event.target.files[0].name);
      formData.append('file', event.target.files[0]);

      const config = {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      };

      api.post(
        '/states',
        formData,
        config,
      ).then(() => this.resetFileInput());
    },
    resetFileInput() {
      this.$refs.fileupload.value = null;
      this.getStates();
    },
  },
};
</script>
