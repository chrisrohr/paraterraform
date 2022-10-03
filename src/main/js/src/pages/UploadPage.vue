<template>
  <q-page>

    <div class="row flex flex-center">
      <h3>Upload Page</h3>
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
    };
  },
  mounted() {
    this.getStates();
  },
  methods: {
    getStates() {
      this.$api.get('/states')
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
        this.$api.delete(`/states/${row.id}`)
          .then(() => this.getStates());
      });
    },
  },
};
</script>
